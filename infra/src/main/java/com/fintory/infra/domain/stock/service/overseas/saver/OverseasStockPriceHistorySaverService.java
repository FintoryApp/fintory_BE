package com.fintory.infra.domain.stock.service.overseas.saver;

import com.fintory.domain.stock.dto.overseas.core.OverseasStockPriceHistory;
import com.fintory.domain.stock.model.IntervalType;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.model.StockPriceHistory;
import com.fintory.infra.domain.stock.repository.StockPriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OverseasStockPriceHistorySaverService {

    private final StockPriceHistoryRepository stockPriceHistoryRepository;

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
}
