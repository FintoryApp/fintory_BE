package com.fintory.websocket.provider.config;

import com.fintory.websocket.provider.handler.KoreanLiveStockPriceWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.Disposable;

import java.net.URI;

@Component
@RequiredArgsConstructor
@Slf4j
public class KoreanWebSocketConnection {
    private final WebSocketClient client;
    private final KoreanLiveStockPriceWebSocketHandler koreanLiveStockPriceWebSocketHandler;
    private Disposable connection;

    private static final String WS_URL = "ws://ops.koreainvestment.com:21000/tryitout/H0STCNT0";

    public void connect(){
        if (connection != null && !connection.isDisposed()) {
            log.warn("한국 투자 증권 WebSocket이 이미 연결되어 있습니다.");
            return;
        }

        connection = client.execute(URI.create(WS_URL), koreanLiveStockPriceWebSocketHandler)
                .subscribe(null, error->{
                    log.error("한국 투자 증권 WebSocket 연결 에러: {}", error.getMessage()); //TODO 재연결 로직 추가
                }, ()-> log.info("한국 투자 증권 WebSocket 연결 종료"));
    }

    public void disconnect(){
        if (connection != null && !connection.isDisposed()) {
            connection.dispose();
            log.info("한국투자증권 WebSocket 연결 해제");
        }
    }

}
