package com.fintory.infra.domain.stock.service.overseas;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.overseas.response.OverseasLiveStockPriceResponse;
import com.fintory.domain.stock.dto.overseas.wrapper.OverseasLiveStockPriceWrapper;
import com.fintory.domain.stock.model.IntervalType;
import com.fintory.domain.stock.model.LiveStockPrice;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.model.StockPriceHistory;
import com.fintory.domain.stock.service.overseas.OverseasLiveStockPriceService;
import com.fintory.infra.domain.stock.repository.LiveStockPriceRepository;
import com.fintory.infra.domain.stock.repository.StockPriceHistoryRepository;
import com.fintory.infra.domain.stock.repository.StockRepository;
import com.fintory.infra.domain.stock.service.overseas.saver.OverseasLiveStockPriceSaverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static com.fintory.domain.stock.dto.overseas.response.OverseasLiveStockPriceResponse.convertFromLiveStockPrice;

@Service
@Slf4j
@RequiredArgsConstructor
public class OverseasLiveStockPriceServiceImpl implements OverseasLiveStockPriceService {

    @Value("${hantu-openapi.appkey}")
    private String appkey;

    @Value("${hantu-openapi.appsecret}")
    private String appsecret;

    @Value("${hantu-openapi.base-url}")
    private String baseUrl;

    private final RedisTemplate<Object, Object> redisTemplate;
    private final StockRepository stockRepository;
    private final LiveStockPriceRepository liveStockPriceRepository;
    private final OverseasLiveStockPriceSaverService liveStockPriceSaverService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final StockPriceHistoryRepository stockPriceHistoryRepository;

    @Override
    @Retryable(maxAttempts=3, backoff = @Backoff(delay = 1000))
    public void initLiveStockPrice(){
        List<Stock> stockList = stockRepository.findByCurrencyName("USD");
        String token = (String) redisTemplate.opsForValue().get("kis-access-token");
        int successCount = 0;

        if (token == null || token.trim().isEmpty()) {
            log.error("KIS 액세스 토큰을 찾을 수 없습니다.");
            throw new DomainException(DomainErrorCode.TOKEN_EMPTY);
        }

        for(Stock stock : stockList) {
            try {
                getLiveStockPriceViaRestAPI(stock.getCode(), token);
                successCount++;
            } catch (Exception e) {
                log.warn("주식 {} 처리 실패: {}", stock.getCode(), e.getMessage());
            }
        }

        //단 하나도 성공하지 못할 경우 -> 시스템적인 에러이므로 재시작 필요
        if(successCount==0){
            log.error("현재가 데이터 초기화 작업 중 종목 처리 실패");
            throw new DomainException(DomainErrorCode.COMPLETE_INITIALIZATION_FAILURE);

        }
    }

    //REST API로 현재가 데이터 조회
    @Override
    public void getLiveStockPriceViaRestAPI(String code, String token){
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .path("/uapi/overseas-price/v1/quotations/price-detail")
                    .queryParam("AUTH", "")
                    .queryParam("EXCD", "NAS")
                    .queryParam("SYMB", code)
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("authorization", "Bearer " + token);
            headers.set("appkey", appkey);
            headers.set("appsecret", appsecret);
            headers.set("tr_id", "HHDFS76200200");
            headers.set("custtype", "P");
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                OverseasLiveStockPriceWrapper wrapper = objectMapper.readValue(response.getBody(), OverseasLiveStockPriceWrapper.class);
                liveStockPriceSaverService.saveLiveStockPrice(code, wrapper.output()); //성공하면 db에 저장
            } else {
                log.error("현재가 데이터 조회 실패: {} - 응답이 비어있음", code);
                throw new DomainException(DomainErrorCode.API_RESPONSE_EMPTY);
            }

        } catch (DomainException e) {
            throw e;
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 실패: {} - {}", code, e.getMessage());
            throw new DomainException(DomainErrorCode.JSON_PARSING_ERROR);
        } catch (ResourceAccessException e) {
            log.error("API 연결 실패: {} - {}", code, e.getMessage());
            throw new DomainException(DomainErrorCode.API_CONNECTION_ERROR);
        } catch (Exception e) {
            log.error("예상치 못한 오류: {} - {}", code, e.getMessage());
            throw new DomainException(DomainErrorCode.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public OverseasLiveStockPriceResponse getLiveStockPriceViaQuery(Stock stock){
        LiveStockPrice liveStockPrice = liveStockPriceRepository.findByStock(stock)
                .orElseThrow(() -> new DomainException(DomainErrorCode.LIVE_STOCK_PRICE_NOT_FOUND));

        //currentPrice openPrice 전달
        StockPriceHistory stockPriceHistory = stockPriceHistoryRepository.findFirstByStockAndIntervalTypeOrderByUpdatedAtDesc(stock, IntervalType.HOURLY)
                .orElse(null); //TODO orElseThrow로 변경

        BigDecimal openPrice = stockPriceHistory!=null ? stockPriceHistory.getOpenPrice() : BigDecimal.ZERO;
        return convertFromLiveStockPrice(liveStockPrice.getCurrentPrice(),openPrice);
    }
}
