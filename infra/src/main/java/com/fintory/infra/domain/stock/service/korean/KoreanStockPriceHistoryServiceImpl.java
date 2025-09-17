package com.fintory.infra.domain.stock.service.korean;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.korean.response.KoreanStockPriceHistoryResponse;
import com.fintory.domain.stock.dto.korean.core.KoreanStockPriceHistory;
import com.fintory.domain.stock.model.IntervalType;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.model.StockPriceHistory;
import com.fintory.domain.stock.service.korean.KoreanStockPriceHistoryService;
import com.fintory.infra.domain.stock.repository.StockPriceHistoryRepository;
import com.fintory.infra.domain.stock.repository.StockRepository;
import com.fintory.infra.domain.stock.service.korean.saver.KoreanStockPriceHistorySaverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class KoreanStockPriceHistoryServiceImpl implements KoreanStockPriceHistoryService {

    private final RedisTemplate<Object, Object> redisTemplate;
    private final StockPriceHistoryRepository stockPriceHistoryRepository;
    private final StockRepository stockRepository;
    private final KoreanStockPriceHistorySaverService  koreanStockPriceHistorySaverService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Retryable(maxAttempts=3, backoff = @Backoff(delay = 1000))
    public void initiateStockPriceHistory(){
        List<Stock> stocks = stockRepository.findByCurrencyName("KRW");
        int successCount = 0;

        for (Stock stock : stocks) {
            try {
                getOverseasStockItemChatPrice3Month(stock);
                sleepSafely();

                getOverseasStockItemChatPriceYear(stock);
                sleepSafely();

                getOverseasStockItemChatPrice5Year(stock);
                sleepSafely();

                getOverseasStockItemChatPriceTotal(stock);
                sleepSafely();

                successCount++;
            } catch (Exception e) {
                e.printStackTrace();
                log.warn("주식 {} 처리 실패: {}", stock.getCode(), e.getMessage());
            }
        }

        // 하나라도 성공하지 못했을 경우만 예외를 던져서 재시작
        if (successCount == 0) {
            log.error("국내주식 기간별 시세 데이터 초기화 작업 중 모든 종목 처리 실패");
            throw new DomainException(DomainErrorCode.COMPLETE_INITIALIZATION_FAILURE);
        }
    }


    public void getOverseasStockItemChatPrice3Month(Stock stock) {
        List<KoreanStockPriceHistory> koreanStockPriceHistories = getKoreanStockItemChatPrice(stock.getCode()+".KS", "1d", "3mo");
        koreanStockPriceHistorySaverService.saveKoreanStockPriceHistory(koreanStockPriceHistories, stock, IntervalType.QUARTERLY);
    }


    public void getOverseasStockItemChatPriceYear(Stock stock) {
        List<KoreanStockPriceHistory> koreanStockPriceHistories = getKoreanStockItemChatPrice(stock.getCode()+".KS", "1wk", "1y");
        koreanStockPriceHistorySaverService.saveKoreanStockPriceHistory(koreanStockPriceHistories, stock, IntervalType.YEARLY);
    }


    public void getOverseasStockItemChatPrice5Year(Stock stock) {
        List<KoreanStockPriceHistory> koreanStockPriceHistories = getKoreanStockItemChatPrice(stock.getCode()+".KS", "1mo", "5y");
        koreanStockPriceHistorySaverService.saveKoreanStockPriceHistory(koreanStockPriceHistories, stock, IntervalType.FIVE_YEARLY);
    }


    public void getOverseasStockItemChatPriceTotal(Stock stock) {
        List<KoreanStockPriceHistory> koreanStockPriceHistories = getKoreanStockItemChatPrice(stock.getCode()+".KS", "3mo", "max");
        koreanStockPriceHistorySaverService.saveKoreanStockPriceHistory(koreanStockPriceHistories, stock, IntervalType.TOTAL);
    }

    // DB에서 기간별 시세 통합 조회
    public KoreanStockPriceHistoryResponse getKoreanStockPriceHistory(String code) {
        Map<String, List<KoreanStockPriceHistory>> chartData = new HashMap<>();
        Stock stock = stockRepository.findByCode(code).orElseThrow(() -> new DomainException(DomainErrorCode.STOCK_NOT_FOUND));

        chartData.put("1D", getKoreanStockPriceHistoryByInterval(stock, IntervalType.HOURLY));
        chartData.put("1W", getFilteredData(stock, IntervalType.QUARTERLY,LocalDate.now().minusWeeks(1)));
        chartData.put("3M", getFilteredData(stock, IntervalType.QUARTERLY, LocalDate.now().minusMonths(3)));
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


    //DB에서 기간별 시세 조회
    private List<KoreanStockPriceHistory> getKoreanStockPriceHistoryByInterval(Stock stock, IntervalType intervalType) {
        List<StockPriceHistory> stockPriceHistories;

        if(intervalType == IntervalType.HOURLY) {
            //기간별 시세 데이터를 updateAt 기준으로 정렬해서 조회
            stockPriceHistories =  stockPriceHistoryRepository.findByStockAndIntervalTypeOrderByUpdatedAtAsc(stock, intervalType);
        }else{
            stockPriceHistories = stockPriceHistoryRepository.findByStockAndIntervalTypeOrderByDateAsc(stock, intervalType);
        }

        return stockPriceHistories.stream()
                .map(this::convertToKoreanStockPriceHistory)
                .toList();
    }

    private KoreanStockPriceHistory convertToKoreanStockPriceHistory(StockPriceHistory priceHistory) {
        return new KoreanStockPriceHistory(
                priceHistory.getOpenPrice(),
                priceHistory.getHighPrice(),
                priceHistory.getLowPrice(),
                priceHistory.getClosePrice(),
                priceHistory.getDate().toString()
        );
    }


    // Yahoo API로부터 해외 주식 기간별 시세 데이터 조회
    //예외 발생 시 initiateStockPriceHistory로 전파
    @Override
    public List<KoreanStockPriceHistory> getKoreanStockItemChatPrice(String code, String interval, String range) {
        try {
            String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + code + "?interval=" + interval + "&range=" + range;

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            headers.set("Accept-Language", "en-US,en;q=0.9");
            headers.set("Connection", "keep-alive");
            headers.set("DNT", "1");
            headers.set("Cache-Control", "no-cache");
            headers.set("Pragma", "no-cache");


            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return processStockChartData(response.getBody());
            } else {
                log.error("차트 데이터 조회 실패: {} - 응답이 비어있음", code);
                throw new DomainException(DomainErrorCode.API_RESPONSE_EMPTY);
            }

        } catch (DomainException e) {
            throw e;
        } catch (ResourceAccessException e) {
            log.error("API 연결 실패: {} - {}", code, e.getMessage());
            throw new DomainException(DomainErrorCode.API_CONNECTION_ERROR);
        } catch (Exception e) {
            log.error("예상치 못한 오류: {} - {}", code, e.getMessage());
            throw new DomainException(DomainErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    //예외가 발생하면 상위 메서드인 getOverseasStockItemChatPrice로 전파
    private List<KoreanStockPriceHistory> processStockChartData(String data) {
        List<KoreanStockPriceHistory> koreanStockPriceHistories = new ArrayList<>();
        try{
            JsonNode node = objectMapper.readTree(data);
            JsonNode result = node.get("chart").get("result").get(0);

            JsonNode timestamps = result.get("timestamp");
            if (timestamps == null || timestamps.size() == 0) {
                return koreanStockPriceHistories;
            }

            JsonNode quote = result.get("indicators").get("quote").get(0);

            if (quote==null) {
                throw new DomainException(DomainErrorCode.API_RESPONSE_EMPTY);
            }

            JsonNode opens = quote.get("open");
            JsonNode closes = quote.get("close");
            JsonNode highs = quote.get("high");
            JsonNode lows = quote.get("low");

            for (int i = 0; i < timestamps.size(); i++) {
                Long timestamp = timestamps.get(i).asLong();
                LocalDate date = Instant.ofEpochSecond(timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                JsonNode openNode = opens.get(i);
                JsonNode highNode = highs.get(i);
                JsonNode lowNode = lows.get(i);
                JsonNode closeNode = closes.get(i);

                if (openNode == null || openNode.isNull() ||
                        highNode == null || highNode.isNull() ||
                        lowNode == null || lowNode.isNull() ||
                        closeNode == null || closeNode.isNull()) {
                    continue;
                }

                KoreanStockPriceHistory koreanStockPriceHistory = new KoreanStockPriceHistory(
                        new BigDecimal(openNode.asText()),
                        new BigDecimal(highNode.asText()),
                        new BigDecimal(lowNode.asText()),
                        new BigDecimal(closeNode.asText()),
                        date.toString()
                );
                koreanStockPriceHistories.add(koreanStockPriceHistory);
            }
        } catch (Exception e) {
            log.error("차트 데이터 처리 중 에러 발생: {}", e.getMessage());
            throw new DomainException(DomainErrorCode.INTERNAL_SERVER_ERROR);
        }
        return koreanStockPriceHistories;
    }

    private void sleepSafely() {
        try {
            Thread.sleep(1000); // 1초로 단축 (또는 더 짧게)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DomainException(DomainErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /*
    @Value("${hantu-openapi.base-url}")
    private String baseUrl;

    @Value("${hantu-openapi.appsecret}")
    private String appsecret;

    @Value("${hantu-openapi.appkey}")
    private String appkey;

    @Override
    //No EntityManager with actual transaction available for current thread - cannot reliably process 'remove' call
    //활성 트랜잭션이 없는 상태에서는 EntityManager가 비활성화된 상태로 -> JPA는 delete와 같은 변경 작업 실행x
    @Retryable(maxAttempts=5, backoff = @Backoff(delay = 1000))
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
                getKoreanStockItemChatPriceWeek(stock);
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

    private void getKoreanStockItemChatPriceWeek(Stock stock) {
        LocalDate before3Month = LocalDate.now().minusWeeks(1);
        LocalDate today = LocalDate.now();
        List<KoreanStockPriceHistory> koreanStockPriceHistories = getKoreanStockItemChatPrice("D", stock.getCode(), before3Month, today, "0");
        log.info(koreanStockPriceHistories.toString());
        koreanStockPriceHistorySaverService.saveKoreanStockPriceHistory(koreanStockPriceHistories, stock, IntervalType.WEEKLY);
    }


    private void getKoreanStockItemChatPrice3Month(Stock stock) {
        LocalDate before3Month = LocalDate.now().minusMonths(3);
        LocalDate today = LocalDate.now();
        List<KoreanStockPriceHistory> koreanStockPriceHistories = getKoreanStockItemChatPrice("W", stock.getCode(), before3Month, today, "0");
        koreanStockPriceHistorySaverService.saveKoreanStockPriceHistory(koreanStockPriceHistories, stock, IntervalType.QUARTERLY);
    }


    private void getKoreanStockItemChatPriceYear(Stock stock) {
        LocalDate beforeYear = LocalDate.now().minusYears(1);
        LocalDate today = LocalDate.now();
        List<KoreanStockPriceHistory> koreanStockPriceHistories = getKoreanStockItemChatPrice("W", stock.getCode(), beforeYear, today, "0");
        koreanStockPriceHistorySaverService.saveKoreanStockPriceHistory(koreanStockPriceHistories, stock, IntervalType.YEARLY);
    }


    private void getKoreanStockItemChatPrice5Year(Stock stock) {
        LocalDate before5Year = LocalDate.now().minusYears(5);
        LocalDate today = LocalDate.now();
        List<KoreanStockPriceHistory> koreanStockPriceHistories = getKoreanStockItemChatPrice("M", stock.getCode(), before5Year, today, "0");
        koreanStockPriceHistorySaverService.saveKoreanStockPriceHistory(koreanStockPriceHistories, stock, IntervalType.FIVE_YEARLY);
    }


    private void getKoreanStockItemChatPriceTotal(Stock stock) {
        LocalDate beforeYear = LocalDate.now().minusYears(20);
        LocalDate today = LocalDate.now();
        List<KoreanStockPriceHistory> koreanStockPriceHistories = getKoreanStockItemChatPrice("Y", stock.getCode(), beforeYear, today, "0");
        koreanStockPriceHistorySaverService.saveKoreanStockPriceHistory(koreanStockPriceHistories, stock, IntervalType.TOTAL);
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
        chartData.put("1D", getKoreanStockPriceHistoryByInterval(stock, IntervalType.DAILY));
        chartData.put("1W", getKoreanStockPriceHistoryByInterval(stock, IntervalType.WEEKLY));
        chartData.put("3M", getKoreanStockPriceHistoryByInterval(stock, IntervalType.QUARTERLY));

        //나머지는 각각의 interval에서
        chartData.put("1Y", getKoreanStockPriceHistoryByInterval(stock, IntervalType.YEARLY));
        chartData.put("5Y", getKoreanStockPriceHistoryByInterval(stock, IntervalType.FIVE_YEARLY));
        chartData.put("total", getKoreanStockPriceHistoryByInterval(stock, IntervalType.TOTAL));

        return new KoreanStockPriceHistoryResponse(stock.getName(), code, chartData);
    }

    //하나의 api로 받은 데이터를 다른 형식으로 보여줘야 할때만 사용
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

        if(intervalType == IntervalType.DAILY) {
            return stockPriceHistory.stream()
                    .sorted(Comparator.comparing(StockPriceHistory::getUpdatedAt))
                    .map(this::convertToKoreanStockPriceHistory)
                    .toList();
        }

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


     */
}