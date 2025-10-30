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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiveStockPriceWebSocketSaverServiceImpl implements LiveStockPriceWebSocketSaverService {

    private final StockPriceHistoryRepository stockPriceHistoryRepository;
    private final StockRepository stockRepository;
    private final LiveStockPriceRepository liveStockPriceRepository;
    private static final Map<String, LocalDateTime> lastCleanupDateByStock = new ConcurrentHashMap<>();
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

        /*
         * 지금 문제 -> 날짜(LocalDate)로만 비교하니까 hourly 데이터 삭제가 안됨(해외 주식은 새벽 5시에 마지막으로 받고 같은 날짜에 10시반에 다시 받기 때문)
         * lastCleanupDate랑 now가 10시간 이상 차이이면 다음날 데이터인 것으로 처리 + hourly 데이터 삭제
         *
         * 날짜가 다를 때 -> 자정 넘어갈 때 이전 데이터 삭제 (날짜 다르면 cleanup 실행)
         * 10시간 이상 차이 -> 해외 주식 특수 케이스(한국 시간 기준 같은날에 2번 거래)
         * */

        LocalDateTime now = LocalDateTime.now();

        // 매번 웹소켓 데이터 저장 시 삭제 쿼리 실행 방지
        LocalDateTime lastCleanupDate = lastCleanupDateByStock.get(stock.getCode());
        boolean shouldCleanup = (lastCleanupDate == null) ||
                (!lastCleanupDate.toLocalDate().equals(now.toLocalDate())) //10시간으로 하면 이전 날짜의 60개 데이터가 지워지지 않음.
                || (Duration.between(lastCleanupDate,now).toHours()>=10);


        if(shouldCleanup) {
            stockPriceHistoryRepository.deleteByStockAndIntervalTypeAndDateBefore(stock, IntervalType.HOURLY, now.toLocalDate());
            lastCleanupDateByStock.put(stock.getCode(), now);
            todayOpenPrices.remove(stock.getCode());
        }

        //오늘날짜 HOURLY 데이터 중에서 updateAt이 가장 오래된 것을 찾아서 closePrice를 새로운 가격으로 업데이트
        List<StockPriceHistory> todayHistories = stockPriceHistoryRepository.findByStockAndIntervalTypeAndDate(stock,IntervalType.HOURLY,now.toLocalDate());

        //60개의 데이터만 저장함
        if(todayHistories.size()<60) {

            BigDecimal openPrice = todayOpenPrices.computeIfAbsent(
                        dto.code(), //key
                        k -> dto.currentPrice()); //값이 없을 때 실행되는 람다

            StockPriceHistory stockPriceHistory = StockPriceHistory.builder()
                    .closePrice(dto.currentPrice())
                    .openPrice(openPrice)
                    .stock(stock)
                    .intervalType(IntervalType.HOURLY)
                    .date(now.toLocalDate())
                    .build();

            stockPriceHistoryRepository.save(stockPriceHistory);
        }else{
            //이미 60개의 데이터가 저장된 경우(저장 시작한지 1시간이 넘은 경우) - 업데이트 방식
            StockPriceHistory stockPriceHistory = stockPriceHistoryRepository.findOldestByStockAndIntervalTypeAndDate(stock,IntervalType.HOURLY,now.toLocalDate())
                    .orElseGet(()->StockPriceHistory.builder()
                            .closePrice(dto.currentPrice())
                            .stock(stock)
                            .intervalType(IntervalType.HOURLY)
                            .date(now.toLocalDate())
                            .build());

            stockPriceHistory =stockPriceHistory.updateStockPriceHistory(dto.currentPrice());

            stockPriceHistoryRepository.save(stockPriceHistory);
        }
    }

}
