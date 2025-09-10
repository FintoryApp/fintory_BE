package com.fintory.infra.domain.stock.service.overseas.saver;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.overseas.core.OverseasLiveStockPrice;
import com.fintory.domain.stock.model.LiveStockPrice;
import com.fintory.domain.stock.model.Stock;
import com.fintory.infra.domain.stock.repository.LiveStockPriceRepository;
import com.fintory.infra.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

//NOTE 외부에서 호출하는 패턴이 아닌 이상 같은 클래스내에서 호출하는 메소드의 경우(self-invocation) 트랜잭션 적용이 안됨. -> 다른 빈으로 분리
// API 호출은 트랜잭션 밖, DB 저장은 트랜잭션 안으로 분리
//REVIEW 동일한 역할로 여러 구현이 생길 일도 없어보이고, saver는 내부 전용으로 보이기에 인터페이스로 분리하지 ㅇ낳았음
@Service
@RequiredArgsConstructor
@Slf4j
public class OverseasLiveStockPriceSaverService {

    private final StockRepository stockRepository;
    private final LiveStockPriceRepository liveStockPriceRepository;

    //현재가 데이터 DB에 저장
    @Transactional
    public void saveLiveStockPrice(String code, OverseasLiveStockPrice priceDto){
        Stock stock = stockRepository.findByCode(code).orElseThrow(()-> new DomainException(DomainErrorCode.STOCK_NOT_FOUND));
        LiveStockPrice liveStockPrice = liveStockPriceRepository.findByStock(stock)
                .orElseGet(() -> LiveStockPrice.builder()
                        .stock(stock)
                        .build());
        BigDecimal currentPrice = priceDto.currentPrice();
        BigDecimal basePrice = priceDto.base();
        BigDecimal priceChange = currentPrice.subtract(basePrice);

        BigDecimal priceChangeRate = basePrice.compareTo(BigDecimal.ZERO) != 0 ?
                priceChange.divide(basePrice, 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        liveStockPrice.updateLiveStockPrice(priceDto.currentPrice(), priceChange, priceChangeRate);
        liveStockPriceRepository.save(liveStockPrice);
    }

}
