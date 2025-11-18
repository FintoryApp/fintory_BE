package com.fintory.websocketwebflux.config;

import com.fintory.websocketwebflux.handler.MockStockWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.Map;
/*
  generator로 부터 실시간 스트림 데이터를 받기위한 웹소켓 핸들러 설정
*/
@Configuration
@RequiredArgsConstructor
public class WebSocketConfig {

    private final MockStockWebSocketHandler mockStockWebSocketHandler;

    @Bean
    public HandlerMapping webSocketHandlerMapping() {
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(Map.of(
                "/ws/mock-stock", mockStockWebSocketHandler  // generator가 데이트를 보낼 엔드포인트 설정 및 핸들러 등록
        ));
        mapping.setOrder(1);
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

}