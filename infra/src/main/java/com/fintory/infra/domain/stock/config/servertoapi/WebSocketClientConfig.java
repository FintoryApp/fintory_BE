package com.fintory.infra.domain.stock.config.servertoapi;

import com.fintory.infra.domain.stock.handler.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

// 서버에서 외부 금융 API 서버로 WebSocket 연결을 위한 클라이언트 설정 코드
@Configuration
public class WebSocketClientConfig {

    @Bean
    @Qualifier("overseasLiveStockPriceWebSocketConnectionManager")
    public WebSocketConnectionManager overseasLiveStockPriceWebSocketConnectionManager( @Qualifier("overseasWebSocketClient") WebSocketClient overseasClient, OverseasLiveStockPriceWebSocketHandler overseasLiveStockPriceWebSocketHandler){
        WebSocketConnectionManager webSocketConnectionManager = new WebSocketConnectionManager(
                overseasClient,
                overseasLiveStockPriceWebSocketHandler,
                "wss://openapi.dbsec.co.kr:7070/websocket"
        );
        webSocketConnectionManager.setAutoStartup(false);
        return webSocketConnectionManager;
    }

    @Bean
    @Primary
    @Qualifier("koreanLiveStockPriceWebSocketConnectionManager")
    public WebSocketConnectionManager koreanLiveStockPriceWebSocketConnectionManager(@Qualifier("koreanWebSocketClient") WebSocketClient koreanClient, KoreanLiveStockPriceWebSocketHandler koreanLiveStockPriceWebSocketHandler){
        WebSocketConnectionManager webSocketCConnectionManager = new WebSocketConnectionManager(
                koreanClient,
                koreanLiveStockPriceWebSocketHandler,
                "ws://ops.koreainvestment.com:21000/tryitout/H0STCNT0"
        );
        webSocketCConnectionManager.setAutoStartup(false);
        return webSocketCConnectionManager;
    }

    @Bean
    public WebSocketClient overseasWebSocketClient() {
        return new StandardWebSocketClient();
    }

    @Bean
    public WebSocketClient koreanWebSocketClient() {
        return new StandardWebSocketClient();
    }
}
