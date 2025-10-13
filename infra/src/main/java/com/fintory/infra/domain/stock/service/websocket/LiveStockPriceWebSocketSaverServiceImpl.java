package com.fintory.infra.domain.stock.service.websocket;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.websocket.LiveStockPriceStream;
import com.fintory.domain.stock.model.IntervalType;
import com.fintory.domain.stock.model.LiveStockPrice;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.model.StockPriceHistory;
import com.fintory.domain.stock.service.websocket.LiveStockPriceWebSocketSaverService;
import com.fintory.infra.domain.stock.repository.LiveStockPriceRepository;
import com.fintory.infra.domain.stock.repository.StockPriceHistoryRepository;
import com.fintory.infra.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LiveStockPriceWebSocketSaverServiceImpl implements LiveStockPriceWebSocketSaverService {

    private final StockPriceHistoryRepository stockPriceHistoryRepository;
    private final StockRepository stockRepository;
    private final LiveStockPriceRepository liveStockPriceRepository;
    private static LocalDate lastCleanupDate=null;
    private static final Map<String, BigDecimal> todayOpenPrices = new ConcurrentHashMap<>();

    //데이터 DB에 저장 메소드
    @Override
    @Transactional
    public void saveStockData(LiveStockPriceStream dto) {
        Stock stock = stockRepository.findByCode(dto.code())
                .orElseThrow(() -> new DomainException(DomainErrorCode.STOCK_NOT_FOUND));

        //NOTE 웹소켓 통신 중에 디비에 저장하지 않아도 된다는 확신이 들때 지우기
        LiveStockPrice liveStockPrice = liveStockPriceRepository.findByStock(stock)
                .orElseGet(() -> LiveStockPrice.builder().stock(stock).build());

        liveStockPrice.updateLiveStockPrice(dto.currentPrice());
        liveStockPriceRepository.save(liveStockPrice);

        //오늘 날짜
        LocalDate now = LocalDate.now();

        // 오늘이 아닌 이전 날짜의 HOURLY 데이터 모두 삭제
        // 매번 웹소켓 데이터 저장 시 삭제 쿼리 실행 방지
        if(!now.equals(lastCleanupDate)) {
            stockPriceHistoryRepository.deleteByStockAndIntervalTypeAndDateBefore(stock, IntervalType.HOURLY, now);
            lastCleanupDate = now;
            todayOpenPrices.clear();
        }

        //오늘날짜 HOURLY 데이터 중에서 updateAt이 가장 오래된 것을 찾아서 closePrice를 새로운 가격으로 업데이트
        List<StockPriceHistory> stockPriceHistories = stockPriceHistoryRepository.findByStockAndIntervalType(stock,IntervalType.HOURLY);

        //60개의 데이터만 저장함
        if(stockPriceHistories.size()<=60) {
                BigDecimal openPrice = todayOpenPrices.computeIfAbsent(
                        dto.code(), //key
                        k -> dto.currentPrice()); //값이 없을 때 실행되는 람다

            StockPriceHistory stockPriceHistory = StockPriceHistory.builder()
                    .closePrice(dto.currentPrice())
                    .openPrice(openPrice)
                    .stock(stock)
                    .intervalType(IntervalType.HOURLY)
                    .date(now)
                    .build();

            stockPriceHistoryRepository.save(stockPriceHistory);
        }else{
            //이미 60개의 데이터가 저장된 경우(저장 시작한지 1시간이 넘은 경우) - 업데이트 방식
            StockPriceHistory stockPriceHistory = stockPriceHistoryRepository.findOldestByStockAndIntervalTypeAndDate(stock,IntervalType.HOURLY,now)
                    .orElseGet(()->StockPriceHistory.builder()
                            .closePrice(dto.currentPrice())
                            .stock(stock)
                            .intervalType(IntervalType.HOURLY)
                            .date(now)
                            .build());

            stockPriceHistory =stockPriceHistory.updateStockPriceHistory(dto.currentPrice());

            stockPriceHistoryRepository.save(stockPriceHistory);
        }
    }

}
