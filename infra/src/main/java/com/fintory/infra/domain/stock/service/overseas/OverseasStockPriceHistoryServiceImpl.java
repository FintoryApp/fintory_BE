package com.fintory.infra.domain.stock.service.overseas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.overseas.core.OverseasStockPriceHistory;
import com.fintory.domain.stock.dto.overseas.response.OverseasStockPriceHistoryResponse;
import com.fintory.domain.stock.model.IntervalType;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.model.StockPriceHistory;
import com.fintory.domain.stock.service.overseas.OverseasStockPriceHistoryService;
import com.fintory.infra.domain.stock.repository.StockPriceHistoryRepository;
import com.fintory.infra.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class OverseasStockPriceHistoryServiceImpl implements OverseasStockPriceHistoryService {

    private final StockPriceHistoryRepository stockPriceHistoryRepository;
    private final StockRepository stockRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Override
    @Transactional
    public void initiateStockPriceHistory(){
        List<Stock> stocks = stockRepository.findByCurrencyName("USD");
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
                log.warn("주식 {} 처리 실패: {}", stock.getCode(), e.getMessage());
            }
        }

        // 하나라도 성공하지 못했을 경우만 예외를 던져서 재시작
        if (successCount == 0) {
            log.error("해외주식 기간별 시세 데이터 초기화 작업 중 모든 종목 처리 실패");
            throw new DomainException(DomainErrorCode.COMPLETE_INITIALIZATION_FAILURE);
        }
    }

    //No EntityManager with actual transaction available for current thread - cannot reliably process 'remove' call
    @Transactional
    public void saveOverseasStockPriceHistory(List<OverseasStockPriceHistory> overseasStockPriceHistoryList, Stock stock, IntervalType intervalType) {
        // 기존 데이터 삭제
        stockPriceHistoryRepository.deleteByStockAndIntervalType(stock, intervalType);

        List<StockPriceHistory> stockPriceHistories = new ArrayList<>();

        for (OverseasStockPriceHistory overseasStockPriceHistory : overseasStockPriceHistoryList) {

            StockPriceHistory stockPriceHistory = StockPriceHistory.builder()
                    .stock(stock)
                    .intervalType(intervalType)
                    .openPrice(overseasStockPriceHistory.openPrice())
                    .highPrice(overseasStockPriceHistory.highPrice())
                    .lowPrice(overseasStockPriceHistory.lowPrice())
                    .closePrice(overseasStockPriceHistory.closePrice())
                    .date(LocalDate.parse(overseasStockPriceHistory.time()))
                    .build();

            stockPriceHistories.add(stockPriceHistory);
        }
        stockPriceHistoryRepository.saveAll(stockPriceHistories);
    }

    @Transactional
    public void getOverseasStockItemChatPrice3Month(Stock stock) {
        List<OverseasStockPriceHistory> overseasStockPriceHistories = getOverseasStockItemChatPrice(stock.getCode(), "1d", "3mo");
        saveOverseasStockPriceHistory(overseasStockPriceHistories, stock, IntervalType.QUARTERLY);
    }

    @Transactional
    public void getOverseasStockItemChatPriceYear(Stock stock) {
        List<OverseasStockPriceHistory> overseasStockPriceHistories = getOverseasStockItemChatPrice(stock.getCode(), "1wk", "1y");
        saveOverseasStockPriceHistory(overseasStockPriceHistories, stock, IntervalType.YEARLY);
    }

    @Transactional
    public void getOverseasStockItemChatPrice5Year(Stock stock) {
        List<OverseasStockPriceHistory> overseasStockPriceHistories = getOverseasStockItemChatPrice(stock.getCode(), "1mo", "5y");
        saveOverseasStockPriceHistory(overseasStockPriceHistories, stock, IntervalType.FIVE_YEARLY);
    }

    @Transactional
    public void getOverseasStockItemChatPriceTotal(Stock stock) {
        List<OverseasStockPriceHistory> overseasStockPriceHistories = getOverseasStockItemChatPrice(stock.getCode(), "3mo", "max");
        saveOverseasStockPriceHistory(overseasStockPriceHistories, stock, IntervalType.TOTAL);
    }

    // DB에서 기간별 시세 통합 조회
    public OverseasStockPriceHistoryResponse getOverseasStockPriceHistory(String code) {
        Map<String, List<OverseasStockPriceHistory>> chartData = new HashMap<>();
        Stock stock = stockRepository.findByCode(code).orElseThrow(() -> new DomainException(DomainErrorCode.STOCK_NOT_FOUND));

        chartData.put("1D", getFilteredData(stock, IntervalType.QUARTERLY ,LocalDate.now()));
        chartData.put("1W", getFilteredData(stock, IntervalType.QUARTERLY,LocalDate.now().minusWeeks(1)));
        chartData.put("3M", getFilteredData(stock, IntervalType.QUARTERLY, LocalDate.now().minusMonths(3)));
        chartData.put("1Y", getOverseasStockPriceHistoryByInterval(stock, IntervalType.YEARLY));
        chartData.put("5Y", getOverseasStockPriceHistoryByInterval(stock, IntervalType.FIVE_YEARLY));
        chartData.put("total", getOverseasStockPriceHistoryByInterval(stock, IntervalType.TOTAL));

        return new OverseasStockPriceHistoryResponse(stock.getName(), code, chartData);
    }


    private List<OverseasStockPriceHistory> getFilteredData(Stock stock, IntervalType intervalType, LocalDate fromDate) {
        List<StockPriceHistory> stockPriceHistory = stockPriceHistoryRepository.findByStockAndIntervalType(stock, intervalType);

        return stockPriceHistory.stream()
                .filter(priceHistory -> !priceHistory.getDate().isBefore(fromDate)) // fromDate 이후 데이터
                .sorted(Comparator.comparing(StockPriceHistory::getDate)) // 날짜순 정렬
                .map(this::convertToOverseasStockPriceHistory)
                .toList();
    }


    //DB에서 기간별 시세 조회
    private List<OverseasStockPriceHistory> getOverseasStockPriceHistoryByInterval(Stock stock, IntervalType intervalType) {
        List<StockPriceHistory> stockPriceHistories = stockPriceHistoryRepository.findByStockAndIntervalType(stock, intervalType);

        return stockPriceHistories.stream()
                .sorted(Comparator.comparing(StockPriceHistory::getDate)) // 날짜순 정렬
                .map(this::convertToOverseasStockPriceHistory)
                .toList();
    }

    private OverseasStockPriceHistory convertToOverseasStockPriceHistory(StockPriceHistory priceHistory) {
        return new OverseasStockPriceHistory(
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
    @Transactional
    public List<OverseasStockPriceHistory> getOverseasStockItemChatPrice(String code, String interval, String range) {
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
    private List<OverseasStockPriceHistory> processStockChartData(String data) {
        List<OverseasStockPriceHistory> overseasStockPriceHistories = new ArrayList<>();
        try{
            JsonNode node = objectMapper.readTree(data);
            JsonNode result = node.get("chart").get("result").get(0);

            JsonNode timestamps = result.get("timestamp");
            if (timestamps == null || timestamps.size() == 0) {
                return overseasStockPriceHistories;
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

                OverseasStockPriceHistory overseasStockPriceHistory = new OverseasStockPriceHistory(
                        new BigDecimal(openNode.asText()),
                        new BigDecimal(highNode.asText()),
                        new BigDecimal(lowNode.asText()),
                        new BigDecimal(closeNode.asText()),
                        date.toString()
                );
                overseasStockPriceHistories.add(overseasStockPriceHistory);
            }
        } catch (Exception e) {
            log.error("차트 데이터 처리 중 에러 발생: {}", e.getMessage());
            throw new DomainException(DomainErrorCode.INTERNAL_SERVER_ERROR);
        }
        return overseasStockPriceHistories;
    }

    private void sleepSafely() {
        try {
            Thread.sleep(1000); // 1초로 단축 (또는 더 짧게)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DomainException(DomainErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

}