package com.fintory.child.domain.stock.controller;

import com.fintory.domain.stock.dto.LiveStockPriceResponse;
import com.fintory.domain.stock.dto.StockMessageRequest;
import com.fintory.infra.domain.stock.service.CreateLiveStockPriceServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Controller //Http ResponseBody가 아닌 WebSocket 메시지 브로드 캐스트용
@RequiredArgsConstructor
@Slf4j
public class StockWebSocketControllerImpl {

    private final SimpMessagingTemplate messagingTemplate;
    private final CreateLiveStockPriceServiceImpl  createLiveStockPriceService;

    private final Set<String> subscribedLivePriceStocks = ConcurrentHashMap.newKeySet();

    @MessageMapping("/stock/subscribe/live-stock")
    public void subscribeLiveStock(StockMessageRequest request){
        subscribedLivePriceStocks.add(request.code());
        sendLivePriceUpdate();
    }

    @MessageMapping("/stock/unsubscribe/live-stock")
    public void unsubscribeLiveStock(StockMessageRequest request){
        subscribedLivePriceStocks.remove(request.code());
    }


    @Scheduled(fixedRate=1000)
    public void sendLivePriceUpdate(){
        for(String stockCode : subscribedLivePriceStocks){
            try{
                LiveStockPriceResponse response = createLiveStockPriceService.createLiveStockPrice(stockCode);

                messagingTemplate.convertAndSend("/topic/stock/live-price/"+stockCode,response);
            }catch(Exception e){
                log.error("현재가 전송 실패:{}",e.getMessage());
            }
        }
    }

}
