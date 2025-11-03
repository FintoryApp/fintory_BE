package com.fintory.domain.common.config;

import com.fintory.domain.stock.service.websocket.LiveStockPriceWebsocketService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

//REVIEW 혹시 해당 파일의 위치를 바꾸길 원하시면 리뷰 주세요! ->Micrometer가 있는 모듈에서 작업하기 위해 해당 위치를 선택함
@Component
@RequiredArgsConstructor
public class WebSocketMetrics {

    private final MeterRegistry meterRegistry;
    private final LiveStockPriceWebsocketService websocketService;
    private final AtomicInteger activeConnections = new AtomicInteger(0);

    @PostConstruct
    public void registerMetrics(){

        // STOMP 활성 연결 수
        Gauge.builder("stomp.connections.active",
                        activeConnections, AtomicInteger::get)
                .description("Active STOMP connections (클라이언트 수)")
                .register(meterRegistry);

        // 국내 주식 활성 구독 종목 수
        Gauge.builder("websocket.korean.subscriptions.active",
                        websocketService, service -> service.getKoreanSubscribedStocks().size())
                .description("Active Korean Stock subscriptions count")
                .register(meterRegistry);

        // 해외 주식 활성 구독 종목 수
        Gauge.builder("websocket.overseas.subscriptions.active",
                        websocketService, service -> service.getOverseasSubscribedStocks().size())
                .description("Active Overseas Stock subscriptions count")
                .register(meterRegistry);

        // 국내 Websocket 연결 상태
        Gauge.builder("websocket.korean.connected",
                        websocketService, service -> service.isKoreanConnected() ? 1.0 : 0.0)
                .description("Korean WebSocket connection status")
                .register(meterRegistry);

        // 해외 Websocket 연결 상태
        Gauge.builder("websocket.overseas.connected",
                        websocketService, service -> service.isOverseasConnected() ? 1.0 : 0.0)
                .description("Overseas WebSocket connection status")
                .register(meterRegistry);

    }

    // 연결 관리
    public void incrementConnection() {
        activeConnections.incrementAndGet();
    }

    public void decrementConnection() {
        activeConnections.decrementAndGet();
    }
}


