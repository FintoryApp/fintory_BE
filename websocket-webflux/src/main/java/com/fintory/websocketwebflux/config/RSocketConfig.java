package com.fintory.websocketwebflux.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
/*
   stomp + broker 를 대체하여 클라이언트에게 브로드캐스팅하기 위한 rsocket 프로토콜 설정 파일
*/
@Configuration
public class RSocketConfig {

    @Bean
    public RSocketMessageHandler rSocketMessageHandler() {
        RSocketMessageHandler handler = new RSocketMessageHandler();
        handler.setRSocketStrategies(rSocketStrategies());
        return handler;
    }

    @Bean
    public RSocketStrategies rSocketStrategies() {
        return RSocketStrategies.builder().build();
    }
}
