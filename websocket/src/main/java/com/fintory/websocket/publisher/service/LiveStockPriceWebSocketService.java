package com.fintory.websocket.publisher.service;


import com.fintory.websocket.provider.service.StockSubscriptionService;
import com.fintory.websocket.provider.service.WebSocketConnectionService;
import com.fintory.websocket.publisher.state.StockDataHolder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.*;



//NOTE 구독 시도시 -> 에러 코드를 보고 프론트에서 DB API 호출
//NOTE 구독 성공 후 일정시간 동안 데이터가 오지 않으면 -> 프론트에서 연결 끊김 판단
@Service
@Slf4j
@RequiredArgsConstructor
public class LiveStockPriceWebSocketService {


    private final StockDataHolder stockDataHolder;
    private final StockSubscriptionService stockSubscriptionService;
    private final StockDataBatchSaveService stockDataBatchSaveService;
    private final WebSocketConnectionService webSocketConnectionService;
    private final MarketTimeService marketTimeService;
    private final RedisTemplate<Object, Object> redisTemplate;

    /* 구독 자동 실행 메소드 */
    @Scheduled(cron="0 30 09 * * MON-FRI", zone="America/New_York")
    public void scheduledOverseasMarketSubscription(){
        stockSubscriptionService.startOverseasMarketSubscription();
    }

    @Scheduled(cron="0 0 9 * * MON-FRI", zone="Asia/Seoul")
    public void scheduledKoreanMarketSubscription(){
        stockSubscriptionService.startKoreanMarketSubscription();
    }


    @PostConstruct
    public void initMarketSubscriptions() {
        // 국내 장 체크 및 구독
        if (marketTimeService.isKoreanMarketOpen()) {
            log.info("애플리케이션 시작 - 국내 장 열림, 자동 구독 시작");
            stockDataHolder.setCachedAccessToken((String) redisTemplate.opsForValue().get("kis-access-token"));
            stockSubscriptionService.startKoreanMarketSubscription();
        } else {
            log.info("국내 장이 열려있지 않아 자동 구독 스킵");
        }

        // 해외 장 체크 및 구독
        if (marketTimeService.isOverseasMarketOpen()) {
            log.info("애플리케이션 시작 - 해외 장 열림, 자동 구독 시작");
            stockDataHolder.setCachedAccessToken((String) redisTemplate.opsForValue().get("db-access-token"));
            stockSubscriptionService.startOverseasMarketSubscription();
        } else {
            log.info("해외 장이 열려있지 않아 자동 구독 스킵");
        }
    }

    /* 스케줄링 - 배치 저장 */
    @Scheduled(cron = "0 * 9-15 * * MON-FRI", zone = "Asia/Seoul")
    public void saveKoreanStockDataBatch() {
        if (!marketTimeService.isKoreanMarketOpen()) {
            return;
        }
        stockDataBatchSaveService.saveBatchData("국내", stockDataHolder.getKoreanPendingData());
    }

    @Scheduled(cron = "0 * 9-15 * * MON-FRI", zone = "America/New_York")
    public void saveOverseasStockDataBatch() {
        if (!marketTimeService.isOverseasMarketOpen()) {
            return;
        }
        stockDataBatchSaveService.saveBatchData("해외", stockDataHolder.getOverseasPendingData());
    }

    /* 스케줄링 - 장 마감 정리 */
    @Scheduled(cron = "0 20 15 * * MON-FRI", zone = "Asia/Seoul")
    public void cleanUpAfterKoreanMarketClose() {
        stockDataBatchSaveService.saveRemainingData("국내", stockDataHolder.getKoreanPendingData());
        webSocketConnectionService.disconnectKoreanWebSocket();
        log.info("국내 장 마감 정리 완료");
    }

    @Scheduled(cron = "0 0 16 * * MON-FRI", zone = "America/New_York")
    public void cleanUpAfterOverseasMarketClose() {
        stockDataBatchSaveService.saveRemainingData("해외", stockDataHolder.getOverseasPendingData());
        webSocketConnectionService.disconnectOverseasWebSocket();
        log.info("해외 장 마감 정리 완료");
    }


    @PreDestroy
    public void cleanUp() {
        try {
            // 남은 데이터 저장
            stockDataBatchSaveService.saveRemainingData("국내", stockDataHolder.getKoreanPendingData());
            stockDataBatchSaveService.saveRemainingData("해외", stockDataHolder.getOverseasPendingData());
                    // 웹소켓 연결 해제
                    if (stockDataHolder.getIsKoreanConnected().get()) {
                        webSocketConnectionService.disconnectKoreanWebSocket();
                    }
                    if (stockDataHolder.getIsOverseasConnected().get()) {
                        webSocketConnectionService.disconnectOverseasWebSocket();
                    }

            // 최종 리소스 정리 -> (안전장치)
            stockDataHolder.getKoreanSubscribedStocks().clear();
            stockDataHolder.getOverseasSubscribedStocks().clear();
            stockDataHolder.getPreviousKoreanData().clear();
            stockDataHolder.getPreviousOverseasData().clear();
            stockDataHolder.getKoreanPendingData().clear();
            stockDataHolder.getOverseasPendingData().clear();

        } catch (Exception e) {
            log.error("WebSocket cleanup 중 에러 발생", e);

            // 에러 발생해도 리소스는 강제 정리
            stockDataHolder.getKoreanSubscribedStocks().clear();
            stockDataHolder.getOverseasSubscribedStocks().clear();
            stockDataHolder.getPreviousKoreanData().clear();
            stockDataHolder.getPreviousOverseasData().clear();
            stockDataHolder.getKoreanPendingData().clear();
            stockDataHolder.getOverseasPendingData().clear();
        }

        log.info("WebSocket 연결 해제 완료");
    }

    /* 메트릭용 Getter 추가  */
    public Set<String> getKoreanSubscribedStocks() {
        return stockDataHolder.getKoreanSubscribedStocks();
    }

    public Set<String> getOverseasSubscribedStocks() {
        return stockDataHolder.getOverseasSubscribedStocks();
    }

    public boolean isKoreanConnected() {
        return stockDataHolder.getIsKoreanConnected().get();
    }

    public boolean isOverseasConnected() {
        return stockDataHolder.getIsOverseasConnected().get();
    }
}