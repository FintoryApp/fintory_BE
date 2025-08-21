package com.fintory.infra.domain.stock.service;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.LiveStockPriceResponse;
import com.fintory.domain.stock.model.LiveStockPrice;
import com.fintory.domain.stock.model.Stock;
import com.fintory.infra.domain.stock.repository.LiveStockPriceRepository;
import com.fintory.infra.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreateLiveStockPriceServiceImpl {

    private final LiveStockPriceRepository liveStockPriceRepository;
    private final StockRepository stockRepository;
    private Random random = new Random(); //TODO 빈으로 관리

    private final long UPDATE_INTERVAL = 3600000; //REVIEW 1시간마다 DB에 저장
    private final AtomicLong lastUpdatedTime= new AtomicLong(0); //메인 메모리에서 직접 접근(가시성 보장) + 원자적 연산 (읽기 + 수정 + 쓰기 하나의 작업 보장)

    public LiveStockPriceResponse createLiveStockPrice(String code){
        long currentTime = System.currentTimeMillis();

        Stock stock = stockRepository.findByCode(code).orElseThrow(() -> new DomainException(DomainErrorCode.STOCK_NOT_FOUND));
        LiveStockPrice liveStockPrice = liveStockPriceRepository.findByStock(stock)
        .orElseGet(()-> LiveStockPrice.builder().stock(stock).build());

        BigDecimal currentPrice = nextPrice(liveStockPrice.getCurrentPrice(),BigDecimal.valueOf(0.05),0.2);

        liveStockPrice.updateLiveStockPrice(currentPrice,liveStockPrice);

        //1시간 마다 데이터를 저장하는 로직
        //NOTE 따로 저장 로직을 만드는 방식 -> 메소드 중복 때문에 현재 같은 메소드에 위치시킴.
        if(currentTime -lastUpdatedTime.get()>=UPDATE_INTERVAL ){
            liveStockPriceRepository.save(liveStockPrice);
            lastUpdatedTime.set(currentTime);
        }

        return new LiveStockPriceResponse(
            liveStockPrice.getCurrentPrice(),
            liveStockPrice.getPriceChange(),
            liveStockPrice.getPriceChangeRate(),
            stock.getName()
        );
    }



    //GBM 기반 계산
    public BigDecimal nextPrice(BigDecimal prevPrice, BigDecimal mu, double sigma){

        //1년 거래를 252 거래일로 계산
        double dt = 1.0/252.0;

        //난수(정규분포)
        double z = random.nextGaussian();

        //drift
        double drift = mu.doubleValue()*dt - 0.5*sigma*sigma*dt;

        //충격 항
        double shock = sigma*Math.sqrt(dt)*z;

        //다음 가격 = 이전가격 * exp(drift + shock)
        double next = prevPrice.doubleValue()*Math.exp(drift+shock);

        return BigDecimal.valueOf(next);
    }
}
