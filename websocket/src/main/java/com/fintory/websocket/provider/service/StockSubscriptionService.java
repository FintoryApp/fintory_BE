package com.fintory.websocket.provider.service;


import com.fintory.domain.stock.model.Stock;
import com.fintory.websocket.provider.handler.KoreanLiveStockPriceWebSocketHandler;
import com.fintory.websocket.provider.handler.OverseasLiveStockPriceWebSocketHandler;
import com.fintory.websocket.publisher.repository.StockRepository;
import com.fintory.websocket.publisher.service.MarketTimeService;
import com.fintory.websocket.publisher.state.StockDataHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class StockSubscriptionService {
    private final StockDataHolder stockDataHolder;
    private final KoreanLiveStockPriceWebSocketHandler koreanHandler;
    private final OverseasLiveStockPriceWebSocketHandler overseasHandler;

    private final WebSocketConnectionService connectionService;
    private final StockRepository stockRepository;
    private final MarketTimeService marketTimeService;

    /*  장 시작 시 자동으로 필요한 종목 전부 구독*/
    public void startKoreanMarketSubscription(){
        List<Stock> targetStocks = stockRepository.findByCurrencyName("KRW");

        if (!marketTimeService.isKoreanMarketOpen()) {
            return;
        }

        connectionService.connectKoreanWebSocket();

        int beforeSize = stockDataHolder.getKoreanSubscribedStocks().size();

        targetStocks.forEach(dto -> {
            if(!stockDataHolder.getKoreanSubscribedStocks().contains(dto.getCode())) {
                try {
                    koreanHandler.subscribe(dto.getCode());
                    stockDataHolder.getKoreanSubscribedStocks().add(dto.getCode());
                }catch (Exception e){
                    log.error("종목 {} 구독 실패: {}", dto.getCode(), e.getMessage());
                }
            }
        });
        int successCount = stockDataHolder.getKoreanSubscribedStocks().size() - beforeSize;
        log.info("장 시작 - 총 {} 종목 중 {} 종목 구독 완료",
                targetStocks.size(), successCount);
    }


    public void startOverseasMarketSubscription(){

        List<Stock> targetStocks = stockRepository.findByCurrencyName("USD");

        if (!marketTimeService.isOverseasMarketOpen()) {
            return;
        }
        connectionService.connectOverseasWebSocket();

        int beforeSize = stockDataHolder.getOverseasSubscribedStocks().size();

        Flux.fromIterable(targetStocks)
            .filter(stock -> !stockDataHolder.getOverseasSubscribedStocks().contains(stock.getCode()))
            .delayElements(Duration.ofSeconds(1)) //최대 호출 횟수(분당 6회) 제한 때문에 추가
            .doOnNext(stock->{
                 overseasHandler.subscribe(stock.getCode());
                 stockDataHolder.getOverseasSubscribedStocks().add(stock.getCode());
                 })
                .doOnError(e->log.error("해외 주식 구독 실패: {}",  e.getMessage()))
                .onErrorResume(e-> Mono.empty())
                .doOnComplete(()->{
                      int successCount = stockDataHolder.getOverseasSubscribedStocks().size() - beforeSize;
                      log.info("장 시작 - 총 {} 종목 중 {} 종목 구독 완료",
                            targetStocks.size(), successCount);
                      })
                .subscribe();
    }
}
