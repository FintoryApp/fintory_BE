package com.fintory.websocket.service;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.websocket.LiveStockPriceStream;
import com.fintory.domain.stock.model.Stock;
import com.fintory.infra.domain.stock.repository.StockRepository;
import com.fintory.websocket.handler.KoreanLiveStockPriceWebSocketHandler;
import com.fintory.websocket.handler.OverseasLiveStockPriceWebSocketHandler;
import com.fintory.websocket.state.StockDataHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

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
            log.info("국내 장이 열려있지 않아 자동 구독 스킵");
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
            log.info("해외 장이 열려있지 않아 자동 구독 스킵");
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
