package com.fintory.infra.domain.stock.service.korean.saver;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.korean.core.KoreanStockRankData;
import com.fintory.domain.stock.dto.korean.wrapper.KoreanStockRankDataWrapper;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.model.StockRank;
import com.fintory.infra.domain.stock.repository.StockRankRepository;
import com.fintory.infra.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class KoreanStockRankSaverService {


    private final StockRepository stockRepository;
    private final StockRankRepository stockRankRepository;

    //순위를 얻는데 필요한 데이터 저장 메서드
    @Transactional
    public void saveStockRankData(String code, KoreanStockRankDataWrapper response) {

        if (response == null || response.output() == null) {
            log.warn("순위 관련 데이터 응답이 비어있음: {}", code);
            throw new DomainException(DomainErrorCode.API_RESPONSE_EMPTY);
        }

        KoreanStockRankData item = response.output();

        if (item == null) {
            log.warn("순위 관련 응답에서 데이터를 찾을 수 없음");
            throw new DomainException(DomainErrorCode.STOCK_DATA_NOT_FOUND);
        }

        StockRank stockRank = stockRankRepository.findByStockCode(code).orElse(null);
        Stock stock = stockRepository.findByCode(code).orElseThrow(()->new DomainException(DomainErrorCode.STOCK_NOT_FOUND));

        if (stockRank == null) {
            stockRank = StockRank.builder()
                    .tradingVolume(item.tradingVolume())
                    .rocRate(item.roc())
                    .marketCap(item.marketCap())
                    .stock(stock)
                    .build();
        } else {
            stockRank.updateStockRankData(item.marketCap(), item.roc(), item.tradingVolume());
        }

        stockRankRepository.save(stockRank);
    }

}
