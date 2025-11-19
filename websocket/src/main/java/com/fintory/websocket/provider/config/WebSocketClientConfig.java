package com.fintory.websocket.provider.config;

import com.fintory.websocket.provider.handler.KoreanLiveStockPriceWebSocketHandler;
import com.fintory.websocket.provider.handler.OverseasLiveStockPriceWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

// 서버에서 외부 금융 API 서버로 WebSocket 연결을 위한 클라이언트 설정 코드
@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebSocketClientConfig {

    private final KoreanLiveStockPriceWebSocketHandler koreanLiveStockPriceWebSocketHandler;
    private final OverseasLiveStockPriceWebSocketHandler overseasLiveStockPriceWebSocketHandler;

    @Bean
    public WebSocketClient reactorNettyWebSocketClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(10));

        return new ReactorNettyWebSocketClient(httpClient);
    }

    @Bean
    public KoreanWebSocketConnection koreanWebSocketConnection(WebSocketClient client) {
        return new KoreanWebSocketConnection(client, koreanLiveStockPriceWebSocketHandler);
    }

    @Bean
    public OverseasWebSocketConnection overseasWebSocketConnection(WebSocketClient client) {
        return new OverseasWebSocketConnection(client, overseasLiveStockPriceWebSocketHandler);
    }
}
