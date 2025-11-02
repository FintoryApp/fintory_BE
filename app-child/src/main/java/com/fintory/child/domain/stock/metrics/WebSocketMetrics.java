package com.fintory.child.domain.stock.metrics;

import com.fintory.domain.stock.service.websocket.LiveStockPriceWebsocketService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;

//REVIEW 혹시 해당 파일의 위치를 바꾸길 원하시면 리뷰 주세요! ->Micrometer가 있는 모듈에서 작업하기 위해 해당 위치를 선택함
@Component
@RequiredArgsConstructor
public class WebSocketMetrics {

    private final MeterRegistry meterRegistry;
    private final LiveStockPriceWebsocketService websocketService;
    private final SimpUserRegistry simpUserRegistry;

    @PostConstruct
    public void registerMetrics(){

        //실제 연결된 클라이언트 수
        Gauge.builder("websocket.clients.connected",
                simpUserRegistry, registry -> registry.getUserCount())
                .description("Number of websocket clients connected")
                .register(meterRegistry);

        //활성 세션 수
        Gauge.builder("websocket.sessions.active",
                simpUserRegistry, registry -> registry.getUsers().stream()
                        .mapToInt(user-> user.getSessions().size())
                        .sum())
                .description("Number of websocket sessions active")
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
}


