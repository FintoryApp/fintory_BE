package com.fintory.websocketwebflux.apitoserver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.Map;

@Configuration
public class WebSocketConfig {

    @Bean
    public HandlerMapping webSocketHandlerMapping() {
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(Map.of(
                "/ws/mock-stock", mockStockHandler()  // ← 엔드포인트
        ));
        mapping.setOrder(1);
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
    public WebSocketHandler mockStockHandler() {
        return session -> session.receive()
                .doOnNext(message -> {
                    String payload = message.getPayloadAsText();
                    System.out.println("EC2 서버 수신: " + payload);
                    // TODO: 나중에 Sinks.Many로 브로드캐스트
                })
                .then();
    }
}