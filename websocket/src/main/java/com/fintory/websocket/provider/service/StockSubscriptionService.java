package com.fintory.websocket.provider.service;


import com.fintory.domain.stock.model.Stock;
import com.fintory.infra.domain.stock.repository.StockRepository;
import com.fintory.websocket.provider.handler.KoreanLiveStockPriceWebSocketHandler;
import com.fintory.websocket.provider.handler.OverseasLiveStockPriceWebSocketHandler;
import com.fintory.websocket.publisher.service.MarketTimeService;
import com.fintory.websocket.publisher.state.StockDataHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

        targetStocks.forEach(stock -> {
            if (!stockDataHolder.getOverseasSubscribedStocks().contains(stock.getCode())) {
                try {
                    overseasHandler.subscribe(stock.getCode());
                    stockDataHolder.getOverseasSubscribedStocks().add(stock.getCode());
                }catch(Exception e){
                    log.error("종목 {} 구독 실패: {}", stock.getCode(), e.getMessage());
                }
            }
        });

        int successCount = stockDataHolder.getOverseasSubscribedStocks().size() - beforeSize;
        log.info("장 시작 - 총 {} 종목 중 {} 종목 구독 완료",
                targetStocks.size(), successCount);
    }


}
