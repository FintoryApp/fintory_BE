package com.fintory.websocket.monitoring.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class SSEMetrics {

    private final MeterRegistry meterRegistry;
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final AtomicInteger activeSubscribers = new AtomicInteger(0);
    private Counter messagesSent;
    private Counter messagesDropped;

    @PostConstruct
    public void registerMetrics() {

        // 1. 활성 SSE 연결 수
        Gauge.builder("sse.connections.active",
                        activeConnections, AtomicInteger::get)
                .description("Active SSE connections")
                .register(meterRegistry);

        // 2. 활성 구독자 수
        Gauge.builder("sse.subscribers.active",
                        activeSubscribers, AtomicInteger::get)
                .description("Active Flux subscribers")
                .register(meterRegistry);

        // 3. 전송된 메시지 수
        this.messagesSent = Counter.builder("sse.messages.sent")
                .description("Total messages sent to clients")
                .register(meterRegistry);

        // 4. 드롭된 메시지 수 (백프레셔)
        this.messagesDropped = Counter.builder("sse.messages.dropped")
                .description("Messages dropped due to backpressure")
                .register(meterRegistry);
    }

    // 연결 관리
    public void incrementConnection() {
        activeConnections.incrementAndGet();
    }

    public void decrementConnection() {
        activeConnections.decrementAndGet();
    }

    // 구독자 관리
    public void incrementSubscriber() {
        activeSubscribers.incrementAndGet();
    }

    public void decrementSubscriber() {
        activeSubscribers.decrementAndGet();
    }

    // 메시지 카운팅
    public void incrementMessageSent() {
        messagesSent.increment();
    }

    public void incrementMessageDropped() {
        messagesDropped.increment();
    }
}