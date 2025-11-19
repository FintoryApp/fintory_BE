package com.fintory.websocket.provider.config;

import com.fintory.websocket.provider.handler.KoreanLiveStockPriceWebSocketHandler;
import com.fintory.websocket.provider.handler.OverseasLiveStockPriceWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.Disposable;

import java.net.URI;

@Component
@RequiredArgsConstructor
@Slf4j
public class OverseasWebSocketConnection {

    private final WebSocketClient client;
    private final OverseasLiveStockPriceWebSocketHandler overseasLiveStockPriceWebSocketHandler;
    private Disposable connection;

    private static final String WS_URL = "wss://openapi.dbsec.co.kr:7070/websocket";

    public void connect() {
        if (connection != null && !connection.isDisposed()) {
            log.warn("DB증권 WebSocket이 이미 연결되어 있습니다.");
            return;
        }

        connection = client.execute(
                URI.create(WS_URL),
                overseasLiveStockPriceWebSocketHandler
        ).subscribe(
                null,
                error -> {
                    log.error("DB증권 WebSocket 연결 에러: {}", error.getMessage());
                    // 재연결 로직 추가 가능
                },
                () -> log.info("DB증권 WebSocket 연결 종료")
        );
    }

    public void disconnect() {
        if (connection != null && !connection.isDisposed()) {
            connection.dispose();
            log.info("DB증권 WebSocket 연결 해제");
        }
    }

    public boolean isConnected() {
        return connection != null && !connection.isDisposed();
    }
}