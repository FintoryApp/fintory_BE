package com.fintory.websocketwebflux.controller;

import com.fintory.websocketwebflux.service.StockRealtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
/*
    클라이언트가 RSocket을 통해 웹소켓 연결 및 구독을 요청하는 컨트롤러
*/
@Controller
@RequiredArgsConstructor
public class StockRSocketController {

    private final StockRealtimeService stockRealtimeService;

    @MessageMapping("stock.subscribe.{code}")
    public Flux<String> subscribe(@DestinationVariable String code) {
        return stockRealtimeService.subscribe(code);
    }
}
