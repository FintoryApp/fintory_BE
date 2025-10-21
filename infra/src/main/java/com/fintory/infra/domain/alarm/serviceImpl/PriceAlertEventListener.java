package com.fintory.infra.domain.alarm.serviceImpl;

import com.fintory.domain.alarm.model.NotificationType;
import com.fintory.domain.alarm.model.PriceAlert;
import com.fintory.domain.alarm.service.AlarmService;
import com.fintory.infra.domain.alarm.event.PriceAlertEvent;
import com.fintory.infra.domain.alarm.repository.PriceAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PriceAlertEventListener {

    private final RedisTemplate<String, Object> redisTemplate;
    private final PriceAlertRepository priceAlertRepository;
    private final AlarmService alarmService;


    @Async("alertExecutor")
    @EventListener
    @Transactional
    public void handlePriceAlert(PriceAlertEvent event){

        String stockCode = event.getStockPriceStream().code();
        BigDecimal currentPrice = event.getStockPriceStream().currentPrice();

        String cachedKey = "priceAlert:"+stockCode;
        List<PriceAlert> priceAlertList = getPriceAlertFromCache(cachedKey,stockCode);

        // 감시가 설정 없음
        if(priceAlertList == null){
            return;
        }
        priceAlertList.forEach(alert->{
            try {
                checkAndSendPriceAlert(alert, stockCode, currentPrice, cachedKey);
            }catch (Exception e){
                log.error("감시가 알림 처리 실패",e);
                //로그만 찍고 계속 진행
            }
            });
    }


    private List<PriceAlert> getPriceAlertFromCache(String cachedKey, String stockCode){
        List<PriceAlert> cached = (List<PriceAlert>) redisTemplate.opsForValue().get(cachedKey);

        if (cached == null || cached.isEmpty()){
            //cache miss - db 조회
            List<PriceAlert> priceAlertList = priceAlertRepository.findByStockCode(stockCode);

            //감시가 목록이 없으면 null 반환
            if(priceAlertList == null || priceAlertList.isEmpty()){
                return null;
            }
            //Redis에서 캐싱 (10분)
            redisTemplate.opsForValue().set(cachedKey,priceAlertList, Duration.ofMinutes(10));

            return priceAlertList;
        }

        return cached;
    }


    private void checkAndSendPriceAlert(PriceAlert priceAlert, String stockCode, BigDecimal currentPrice, String cachedKey){


        BigDecimal rangeMultiplier = BigDecimal.valueOf(0.01);
        BigDecimal range = priceAlert.getTargetPrice().multiply(rangeMultiplier);

        BigDecimal minPrice = priceAlert.getTargetPrice().subtract(range);
        BigDecimal maxPrice = priceAlert.getTargetPrice().add(range);

        //범위 체크
        boolean isInRange = currentPrice.compareTo(minPrice) >= 0
                && currentPrice.compareTo(maxPrice) <= 0;

        if (!isInRange) {
            return; // 범위 밖
        }

        // 알림 발송
        String message = String.format(
                "%s이(가) 감시가 %,d원 근처에 도달했습니다! (현재가: %s원)",
                priceAlert.getStock().getName(),
                priceAlert.getTargetPrice().intValue(),
                currentPrice.intValue()
        );

        alarmService.pushMessage(
                priceAlert.getChild().getId(),
                NotificationType.PRICE_ALERT,
                "감시가 알림",
                message
        );

        // REVIEW 1회성 처리: 삭제
        priceAlertRepository.delete(priceAlert);

        // Redis 캐시 무효화
        redisTemplate.delete(cachedKey);

        log.info("priceAlert 발송 : childId = {}, stock={}, targetPrice={}원, currentPrice={}원",
                priceAlert.getChild().getId(), stockCode, priceAlert.getTargetPrice(), currentPrice);

    }
}
