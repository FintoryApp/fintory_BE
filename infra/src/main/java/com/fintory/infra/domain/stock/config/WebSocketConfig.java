package com.fintory.infra.domain.stock.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //현재가 전용 WebSocket
        registry.addEndpoint("/ws/live-price")
                .setAllowedOriginPatterns("*") //TODO 나중에 실제 도메인으로 변경
                .withSockJS(); //자동 핸드셰이크 처리 (HTTP 요청을 받아서 -> ws://로 upgrade)

        // 호가 전용 WebSocket
        registry.addEndpoint("/ws/order-book")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry){
        registry.enableSimpleBroker("/topic/stock"); // 서버 -> 클라이언트로 메시지 보내는 경로 (메시지 브로커가 처리 )
        registry.setApplicationDestinationPrefixes("/app"); // 클라이언트 -> 서버로 메시지를 받는 경로 (어플리케이션이 처리 )
    }


}
