package com.fintory.infra.domain.stock.service.overseas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.overseas.core.OverseasStockPriceHistory;
import com.fintory.domain.stock.dto.overseas.response.OverseasStockPriceHistoryResponse;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.model.StockPriceHistory;
import com.fintory.domain.stock.service.overseas.OverseasStockPriceHistoryService;
import com.fintory.infra.domain.stock.repository.StockPriceHistoryRepository;
import com.fintory.infra.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




@Service
@Slf4j
@RequiredArgsConstructor
public class OverseasStockPriceHistoryServiceImpl implements OverseasStockPriceHistoryService {

    private final StockPriceHistoryRepository stockPriceHistoryRepository;
    private final StockRepository stockRepository;
    private final ObjectMapper objectMapper;

    @Qualifier("yahooWebClient")
    private final WebClient yahooWebClient;

    @Override
    @Transactional
    public void saveStockPriceHistory(){
        try {
            List<Stock> stocks = stockRepository.findByCurrencyName("USD");

            for (Stock stock : stocks) {
                List<OverseasStockPriceHistory> overseasStockPriceHistories = getOverseasStockItemChartPriceDay(stock.getCode());

                if (overseasStockPriceHistories == null || overseasStockPriceHistories.isEmpty()) {
                    continue;
                }

                OverseasStockPriceHistory overseasStockPriceHistory = overseasStockPriceHistories.get(0);
                StockPriceHistory oldStockPriceHistory = stockPriceHistoryRepository.findByStock(stock).orElseGet(()-> StockPriceHistory.builder()
                        .stock(stock)
                        .build());
                oldStockPriceHistory.updateStockPriceHistory(
                        overseasStockPriceHistory.openPrice(),
                        overseasStockPriceHistory.highPrice(),
                        overseasStockPriceHistory.lowPrice(),
                        overseasStockPriceHistory.closePrice()
                      );

                stockPriceHistoryRepository.save(oldStockPriceHistory);
            }
        }catch (Exception e) {
            log.error("해외 주식 saveStockPriceHistory 전체 실행 중 오류 발생", e);
            throw new DomainException(DomainErrorCode.STOCK_PRICE_HISTORY_SAVE_FAILED);
        }
    }


    private List<OverseasStockPriceHistory> processStockChartData(String data)  {

        List<OverseasStockPriceHistory> overseasStockPriceHistories = new ArrayList<>();
        try {
            JsonNode node = objectMapper.readTree(data);
            JsonNode result = node.get("chart").get("result").get(0);

            JsonNode timestamps = result.get("timestamp");

            JsonNode quote = result.get("indicators").get("quote").get(0);
            JsonNode opens = quote.get("open");
            JsonNode closes = quote.get("close");
            JsonNode highs = quote.get("high");
            JsonNode lows = quote.get("low");

            for (int i = 0; i < timestamps.size(); i++) {
                Long timestamp = timestamps.get(i).asLong();
                LocalDate date = Instant.ofEpochSecond(timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                OverseasStockPriceHistory overseasStockPriceHistory = new OverseasStockPriceHistory(
                        new BigDecimal(opens.get(i).asText()),
                        new BigDecimal(highs.get(i).asText()),
                        new BigDecimal(lows.get(i).asText()),
                        new BigDecimal(closes.get(i).asText()),
                        date.toString()
                );
                overseasStockPriceHistories.add(overseasStockPriceHistory);
            }
        }catch (Exception e){
            log.error("차트 데이터 처리 중 에러 발생: {}",e.getMessage());
        }
        return overseasStockPriceHistories;
    }

    //프론트에게 전달할 데이터 생성 메서드
    public OverseasStockPriceHistoryResponse getOverseasStockPriceHistory(String code){
        Map<String,List<OverseasStockPriceHistory>> chartData = new HashMap<>();
        Stock stock = stockRepository.findByCode(code).orElseThrow(()-> new DomainException(DomainErrorCode.STOCK_NOT_FOUND));

        chartData.put("1D", getOverseasStockItemChartPriceDay(code));
        chartData.put("1W", getOverseasStockItemChatPriceWeek(code));
        chartData.put("3M", getOverseasStockItemChatPrice3Month(code));
        chartData.put("1Y", getOverseasStockItemChatPriceYear(code));
        chartData.put("5Y", getOverseasStockItemChatPrice5Year(code));
        chartData.put("total", getOverseasStockItemChatPriceTotal(code));

        return new OverseasStockPriceHistoryResponse(stock.getName(),code,chartData);
    }

    /*
     *
     * 차트 데이터 조회
     *
     * */

    private List<OverseasStockPriceHistory> getOverseasStockItemChartPriceDay(String code) {
        return getOverseasStockItemChatPrice(code, "1h", "1d");
    }


    private List<OverseasStockPriceHistory> getOverseasStockItemChatPriceWeek(String code) {
        return getOverseasStockItemChatPrice(code, "1d", "1w");
    }

    private List<OverseasStockPriceHistory> getOverseasStockItemChatPrice3Month(String code) {
        return getOverseasStockItemChatPrice(code, "1d", "3mo");
    }

    private List<OverseasStockPriceHistory> getOverseasStockItemChatPriceYear(String code) {
        return getOverseasStockItemChatPrice(code, "1mo", "1y");
    }

    private List<OverseasStockPriceHistory> getOverseasStockItemChatPrice5Year(String code) {
        return  getOverseasStockItemChatPrice(code, "1mo", "5y");
    }

    private List<OverseasStockPriceHistory> getOverseasStockItemChatPriceTotal(String code) {
        return  getOverseasStockItemChatPrice(code, "1y", "max");
    }

    @Override
    @Transactional
    public List<OverseasStockPriceHistory> getOverseasStockItemChatPrice(String code, String interval, String range){
        String data = yahooWebClient.get()
                .uri("https://query1.finance.yahoo.com/v8/finance/chart/{symbol}?interval={interval}&range={range}",
                        code, interval,range)
                .exchangeToMono(response -> response.bodyToMono(String.class))
                .block();

        return processStockChartData(data);
    }


}
