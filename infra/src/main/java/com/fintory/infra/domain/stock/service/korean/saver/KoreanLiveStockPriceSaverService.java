package com.fintory.infra.domain.stock.service.korean.saver;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.korean.core.KoreanLiveStockPrice;
import com.fintory.domain.stock.model.LiveStockPrice;
import com.fintory.domain.stock.model.Stock;
import com.fintory.infra.domain.stock.repository.LiveStockPriceRepository;
import com.fintory.infra.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class KoreanLiveStockPriceSaverService {

    private final StockRepository stockRepository;
    private final LiveStockPriceRepository liveStockPriceRepository;

    //현재가 데이터 저장 메소드
    @Transactional
    public void saveLiveStockPrice(String code, KoreanLiveStockPrice priceDto){
        Stock stock = stockRepository.findByCode(code).orElseThrow(()-> new DomainException(DomainErrorCode.STOCK_NOT_FOUND));
        LiveStockPrice liveStockPrice = liveStockPriceRepository.findByStock(stock)
                .orElseGet(() -> LiveStockPrice.builder()
                        .stock(stock)
                        .build());
        liveStockPrice.updateLiveStockPrice(priceDto.currentPrice());
        liveStockPriceRepository.save(liveStockPrice);
    }
}
