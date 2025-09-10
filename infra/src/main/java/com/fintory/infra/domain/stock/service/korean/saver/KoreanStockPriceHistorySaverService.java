package com.fintory.infra.domain.stock.service.korean.saver;

import com.fintory.domain.stock.dto.korean.core.KoreanStockPriceHistory;
import com.fintory.domain.stock.model.IntervalType;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.model.StockPriceHistory;
import com.fintory.infra.domain.stock.repository.StockPriceHistoryRepository;
import com.fintory.infra.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class KoreanStockPriceHistorySaverService {

    private final StockPriceHistoryRepository stockPriceHistoryRepository;
    private final StockRepository stockRepository;

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
}
