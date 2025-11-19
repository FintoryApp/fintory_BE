package com.fintory.websocketwebflux.handler;

import com.fintory.websocketwebflux.service.StockRealtimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
/*
    제너레이터로부터 비동기로 스트림 데이터를 받기 위한 핸들러.
    받아서 클라이언트에게 브로드캐스팅할 Rsocket으로 전달
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MockStockWebSocketHandler implements WebSocketHandler {

    private final StockRealtimeService stockRealtimeService;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .doOnNext(stockRealtimeService::publish)
                .then();
    }

}
