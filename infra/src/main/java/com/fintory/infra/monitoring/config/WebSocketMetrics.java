package com.fintory.infra.monitoring.config;

import com.fintory.domain.stock.service.websocket.LiveStockPriceWebsocketService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

//REVIEW 혹시 해당 파일의 위치를 바꾸길 원하시면 리뷰 주세요! ->WebSocketMetrics는 Micrometer와 Prometheus 같은 외부 기술에 의존하기 때문에 infra 모듈에 위치시켰습니다
@Component
public class WebSocketMetrics {

    private final LiveStockPriceWebsocketService websocketService;
    private final MeterRegistry meterRegistry;
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private Counter messageSent;

    //REVIEW @Lazy를 쓰기 위해 명시적 생성자 사용 -> @Lazy는 생성자 파라미터에 직접 붙어 있어야 동작함
    // @RequiredConstructor는 생성자 파라미터별 어노테이션을 직접 지원하지 않는 것으로 알고 있음.
    public WebSocketMetrics(@Lazy LiveStockPriceWebsocketService websocketService, MeterRegistry meterRegistry) {
        this.websocketService = websocketService;
        this.meterRegistry = meterRegistry;
    }

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

        this.messageSent = Counter.builder("websocket.messages.sent")
                .description("Messages sent to Front")
                .register(meterRegistry);
    }

    // 연결 관리
    public void incrementConnection() {
        activeConnections.incrementAndGet();
    }

    public void decrementConnection() {
        activeConnections.decrementAndGet();
    }

    public void incrementMessageSent(){
        messageSent.increment();
    }
}


