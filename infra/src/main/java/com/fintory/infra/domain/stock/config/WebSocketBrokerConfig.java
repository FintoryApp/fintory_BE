package com.fintory.infra.domain.stock.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

//클라이언트들이 내 서버에 연결하도록 설정하는 코드
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // 서버 -> 클라이언트
        config.setApplicationDestinationPrefixes("/app"); //클라이언트 -> 서버
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // 초기 웹소켓 연결을 위한 경로
                .setAllowedOriginPatterns("*") //cors 설정
                .withSockJS(); //구형 브라우저를 위한 폴백
    }
}
