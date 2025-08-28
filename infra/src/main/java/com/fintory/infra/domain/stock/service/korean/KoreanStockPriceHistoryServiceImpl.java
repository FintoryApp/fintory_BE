package com.fintory.infra.domain.stock.service.korean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.korean.response.KoreanLiveStockPriceResponse;
import com.fintory.domain.stock.dto.korean.response.KoreanStockPriceHistoryResponse;
import com.fintory.domain.stock.dto.korean.core.KoreanStockPriceHistory;
import com.fintory.domain.stock.dto.korean.wrapper.KoreanStockPriceHistoryWrapper;
import com.fintory.domain.stock.model.IntervalType;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.model.StockPriceHistory;
import com.fintory.domain.stock.service.korean.KoreanStockPriceHistoryService;
import com.fintory.infra.domain.stock.repository.StockPriceHistoryRepository;
import com.fintory.infra.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class KoreanStockPriceHistoryServiceImpl implements KoreanStockPriceHistoryService {

    private final RedisTemplate<Object, Object> redisTemplate;
    private final StockPriceHistoryRepository stockPriceHistoryRepository;
    private final StockRepository stockRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${hantu-openapi.base-url}")
    private String baseUrl;

    @Value("${hantu-openapi.appsecret}")
    private String appsecret;

    @Value("${hantu-openapi.appkey}")
    private String appkey;

    @Override
    @Transactional
    //No EntityManager with actual transaction available for current thread - cannot reliably process 'remove' call
    //활성 트랜잭션이 없는 상태에서는 EntityManager가 비활성화된 상태로 -> JPA는 delete와 같은 변경 작업 실행x
    public void initiateStockPriceHistory() {
        List<Stock> stocks = stockRepository.findByCurrencyName("KRW");
        String token = (String) redisTemplate.opsForValue().get("kis-access-token");
        int successCount = 0;

        if (token == null || token.trim().isEmpty()) {
            log.error("KIS 액세스 토큰을 찾을 수 없습니다.");
            throw new DomainException(DomainErrorCode.TOKEN_EMPTY);
        }

        for (Stock stock : stocks) {
            try {
                getKoreanStockItemChatPrice3Month(stock);
                getKoreanStockItemChatPriceYear(stock);
                getKoreanStockItemChatPrice5Year(stock);
                getKoreanStockItemChatPriceTotal(stock);

                successCount++;
            } catch (Exception e) {
                log.warn("주식 {} 처리 실패: {}", stock.getCode(), e.getMessage()); //로그 기록 남기기
            }
        }
        //하나라도 성공하지 못했을 경우 재시작
        if (successCount == 0) {
            log.error("기간별 시세 데이터 초기화 작업 중 모든 종목 처리 실패");
            throw new DomainException(DomainErrorCode.COMPLETE_INITIALIZATION_FAILURE);

        }
    }

    //DB 저장 템플릿 메서드
    @Transactional
    public void saveKoreanStockPriceHistory(List<KoreanStockPriceHistory> koreanStockPriceHistoryList, Stock stock, IntervalType intervalType) {
        //update가 아닌 기존 데이터 삭제 -> 아니면 덮어씌워짐
        List<StockPriceHistory> existing = stockPriceHistoryRepository.findByStockAndIntervalType(stock, intervalType);
        if (!existing.isEmpty()) {
            stockPriceHistoryRepository.deleteByStockAndIntervalType(stock, intervalType);
        }
        List<StockPriceHistory> koreanStockPriceHistories = new ArrayList<>();


        for (KoreanStockPriceHistory koreanStockPriceHistory : koreanStockPriceHistoryList) {
            LocalDate date = LocalDate.parse(
                    koreanStockPriceHistory.time(),
                    DateTimeFormatter.ofPattern("yyyyMMdd")
            );

            StockPriceHistory stockPriceHistory = StockPriceHistory.builder()
                    .stock(stock)
                    .intervalType(intervalType) // 모든 레코드에 같은 값
                    .openPrice(koreanStockPriceHistory.openPrice())
                    .highPrice(koreanStockPriceHistory.highPrice())
                    .lowPrice(koreanStockPriceHistory.lowPrice())
                    .closePrice(koreanStockPriceHistory.closePrice())
                    .date(date)
                    .build();

            koreanStockPriceHistories.add(stockPriceHistory);

        }
        stockPriceHistoryRepository.saveAll(koreanStockPriceHistories);
    }


    private void getKoreanStockItemChatPrice3Month(Stock stock) {
        LocalDate before3Month = LocalDate.now().minusMonths(3);
        LocalDate today = LocalDate.now();
        List<KoreanStockPriceHistory> koreanStockPriceHistories = getKoreanStockItemChatPrice("D", stock.getCode(), before3Month, today, "0");
        saveKoreanStockPriceHistory(koreanStockPriceHistories, stock, IntervalType.QUARTERLY);
    }


    private void getKoreanStockItemChatPriceYear(Stock stock) {
        LocalDate beforeYear = LocalDate.now().minusYears(1);
        LocalDate today = LocalDate.now();
        List<KoreanStockPriceHistory> koreanStockPriceHistories = getKoreanStockItemChatPrice("W", stock.getCode(), beforeYear, today, "1");
        saveKoreanStockPriceHistory(koreanStockPriceHistories, stock, IntervalType.YEARLY);
    }


    private void getKoreanStockItemChatPrice5Year(Stock stock) {
        LocalDate before5Year = LocalDate.now().minusYears(5);
        LocalDate today = LocalDate.now();
        List<KoreanStockPriceHistory> koreanStockPriceHistories = getKoreanStockItemChatPrice("M", stock.getCode(), before5Year, today, "1");
        saveKoreanStockPriceHistory(koreanStockPriceHistories, stock, IntervalType.FIVE_YEARLY);
    }


    private void getKoreanStockItemChatPriceTotal(Stock stock) {
        LocalDate beforeYear = LocalDate.now().minusYears(20);
        LocalDate today = LocalDate.now();
        List<KoreanStockPriceHistory> koreanStockPriceHistories = getKoreanStockItemChatPrice("Y", stock.getCode(), beforeYear, today, "1");
        saveKoreanStockPriceHistory(koreanStockPriceHistories, stock, IntervalType.TOTAL);
    }

    @Override
    public List<KoreanStockPriceHistory> getKoreanStockItemChatPrice(String unit, String code, LocalDate localDate1, LocalDate localDate2, String orgAdjPrc) {
        try {
            String token = (String) redisTemplate.opsForValue().get("kis-access-token");

            String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .path("/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice")
                    .queryParam("FID_COND_MRKT_DIV_CODE", "UN")
                    .queryParam("FID_INPUT_ISCD", code)
                    .queryParam("FID_INPUT_DATE_1", localDate1)
                    .queryParam("FID_INPUT_DATE_2", localDate2)
                    .queryParam("FID_PERIOD_DIV_CODE", unit)
                    .queryParam("FID_ORG_ADJ_PRC", orgAdjPrc)
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("authorization", "Bearer " + token);
            headers.set("appkey", appkey);
            headers.set("appsecret", appsecret);
            headers.set("tr_id", "FHKST03010100");
            headers.set("custtype", "P");
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                KoreanStockPriceHistoryWrapper wrapper = objectMapper.readValue(response.getBody(), KoreanStockPriceHistoryWrapper.class);
                return wrapper.output();
            } else {
                log.error("차트 데이터 조회 실패: {} - 응답이 비어있음", code);
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

    //DB에서 기간별 시세 데이터 조회 후 처리
    public KoreanStockPriceHistoryResponse getKoreanStockPriceHistory(String code) {
        Map<String, List<KoreanStockPriceHistory>> chartData = new HashMap<>();
        Stock stock = stockRepository.findByCode(code).orElseThrow(() -> new DomainException(DomainErrorCode.STOCK_NOT_FOUND));

        //3개월 일봉 데이터에서 각 기간별로 필터링
        chartData.put("1D", getFilteredData(stock, IntervalType.QUARTERLY, LocalDate.now()));
        chartData.put("1W", getFilteredData(stock, IntervalType.QUARTERLY, LocalDate.now().minusWeeks(1)));
        chartData.put("3M", getFilteredData(stock, IntervalType.QUARTERLY, LocalDate.now().minusMonths(3)));

        //나머지는 각각의 interval에서
        chartData.put("1Y", getKoreanStockPriceHistoryByInterval(stock, IntervalType.YEARLY));
        chartData.put("5Y", getKoreanStockPriceHistoryByInterval(stock, IntervalType.FIVE_YEARLY));
        chartData.put("total", getKoreanStockPriceHistoryByInterval(stock, IntervalType.TOTAL));

        return new KoreanStockPriceHistoryResponse(stock.getName(), code, chartData);
    }

    private List<KoreanStockPriceHistory> getFilteredData(Stock stock, IntervalType intervalType, LocalDate fromDate) {
        List<StockPriceHistory> stockPriceHistory = stockPriceHistoryRepository.findByStockAndIntervalType(stock, intervalType);

        return stockPriceHistory.stream()
                .filter(priceHistory -> !priceHistory.getDate().isBefore(fromDate)) // fromDate 이후 데이터
                .sorted(Comparator.comparing(StockPriceHistory::getDate)) // 날짜순 정렬
                .map(this::convertToKoreanStockPriceHistory)
                .toList();
    }

    private List<KoreanStockPriceHistory> getKoreanStockPriceHistoryByInterval(Stock stock, IntervalType intervalType) {
        List<StockPriceHistory> stockPriceHistory = stockPriceHistoryRepository.findByStockAndIntervalType(stock, intervalType);

        return stockPriceHistory.stream()
                .sorted(Comparator.comparing(StockPriceHistory::getDate))
                .map(this::convertToKoreanStockPriceHistory)
                .toList();

    }

    private KoreanStockPriceHistory convertToKoreanStockPriceHistory(StockPriceHistory priceHistory) {
        String dateString = priceHistory.getDate()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return new KoreanStockPriceHistory(
                priceHistory.getOpenPrice(),
                priceHistory.getHighPrice(),
                priceHistory.getLowPrice(),
                priceHistory.getClosePrice(),
                dateString
        );
    }
}