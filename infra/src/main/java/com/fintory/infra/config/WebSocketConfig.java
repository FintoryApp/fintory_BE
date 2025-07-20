package com.fintory.infra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket //WebSocket 기능 활성화
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new RealPriceWebSocketHandler(),"/stock/ws/realprice")
                .setAllowedOrigins("*");
        registry.addHandler(new PriceQuoteWebSocketHandler(),"/stock/ws/pricequote")
                .setAllowedOrigins("*");
        registry.addHandler(new OverseasRealPriceWebSocketHandler(),"/stock/ws/overseas/realprice")
                .setAllowedOrigins("*");

    }
    
   
}
