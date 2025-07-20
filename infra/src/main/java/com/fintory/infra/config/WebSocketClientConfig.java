package com.fintory.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

@Configuration
public class WebSocketClientConfig {

    @Bean
    public WebSocketConnectionManager kisWebSocketConnectionManager(RealPriceWebSocketHandler realPriceWebSocketHandler){
        WebSocketConnectionManager webSocketConnectionManager = new WebSocketConnectionManager(
                webSocketClient(),
                realPriceWebSocketHandler,
                "ws://ops.koreainvestment.com:21000/tryitout/H0STCNT0"
        );
        webSocketConnectionManager.setAutoStartup(true); //어플리케이션 시작 시 자동 연결
        return webSocketConnectionManager;
    }

    @Bean
    public WebSocketConnectionManager priceQuoteWebSocketConnectionManager(PriceQuoteWebSocketHandler priceQuoteWebSocketHandler){
        WebSocketConnectionManager webSocketConnectionManager = new WebSocketConnectionManager(
                webSocketClient(),
                priceQuoteWebSocketHandler,
                "ws://ops.koreainvestment.com:21000/tryitout/H0STOAA0"
        );
        webSocketConnectionManager.setAutoStartup(true);
        return webSocketConnectionManager;
    }

    @Bean
    public WebSocketConnectionManager overseasRealPriceWebSocketConnectionManager(OverseasRealPriceWebSocketHandler realPriceWebSocketHandler){
        WebSocketConnectionManager webSocketConnectionManager = new WebSocketConnectionManager(
                webSocketClient(),
                realPriceWebSocketHandler,
                "ws://ops.koreainvestment.com:21000/tryitout/HDFSCNT0"
        );
        webSocketConnectionManager.setAutoStartup(true);
        return webSocketConnectionManager;
    }

    @Bean
    public WebSocketClient webSocketClient(){
        return new StandardWebSocketClient();
    }

}
