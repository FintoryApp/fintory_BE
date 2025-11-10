package com.fintory.websocket.monitoring.config;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;
import org.springframework.web.socket.messaging.StompSubProtocolHandler;
import org.springframework.web.socket.messaging.SubProtocolWebSocketHandler;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class StompStatsMetricsConfiguration {

    private final WebSocketMessageBrokerStats stats;
    private final MeterRegistry registry;

    public StompStatsMetricsConfiguration(WebSocketMessageBrokerStats stats, MeterRegistry registry) {
        this.stats = stats;
        this.registry = registry;
        registerMetrics();
    }

    private void registerMetrics() {
        // 1. WebSocket 세션 통계 (Gauge)
        registerSessionMetrics();

        // 2. STOMP 프로토콜 통계 (FunctionCounter)
        registerStompProtocolMetrics();

        // 3. 채널 Executor 큐 통계 (Gauge)
        registerChannelExecutorMetrics();
    }

    // --- 1. WebSocket 세션 통계 등록 ---
    private void registerSessionMetrics() {
        final String sessionsGaugeName = "websocket.sessions";
        final String sessionsGaugeNameTotal = "websocket.total.sessions";

        // 현재 활성 세션 수 (Gauge)
        registry.gauge(sessionsGaugeName, Arrays.asList(Tag.of("status", "current"), Tag.of("type", "total")),
                this.stats, s -> {
                    SubProtocolWebSocketHandler.Stats sessionStats = s.getWebSocketSessionStats();
                    return (sessionStats != null) ? sessionStats.getWebSocketSessions() : 0;
                });

        // 누적 총 세션 수 (FunctionCounter)
        FunctionCounter.builder(sessionsGaugeNameTotal, this.stats, s -> {
                    SubProtocolWebSocketHandler.Stats sessionStats = s.getWebSocketSessionStats();
                    return (sessionStats != null) ? sessionStats.getTotalSessions() : 0;
                })
                .description("Total number of WebSocket sessions (accumulated)")
                .tag("type", "total_accumulated")
                .register(registry);

        // 전송 오류로 종료된 세션 수 (FunctionCounter)
        FunctionCounter.builder(sessionsGaugeName + ".closed.abnormally", this.stats, s -> {
                    SubProtocolWebSocketHandler.Stats sessionStats = s.getWebSocketSessionStats();
                    return (sessionStats != null) ? sessionStats.getTransportErrorSessions() : 0;
                })
                .description("Number of sessions closed due to transport error")
                .tag("reason", "transport_error")
                .register(registry);
    }

    // --- 2. STOMP 프로토콜 통계 등록 ---
    private void registerStompProtocolMetrics() {
        final String metricName = "stomp.messages.processed";

        // CONNECT 프레임 처리 수 (FunctionCounter)
        FunctionCounter.builder(metricName, this.stats, s -> {
                    StompSubProtocolHandler.Stats stompStats = s.getStompSubProtocolStats();
                    return (stompStats != null) ? stompStats.getTotalConnect() : 0;
                })
                .description("Total number of STOMP CONNECT frames processed")
                .tag("action", "CONNECT")
                .register(registry);

        // CONNECTED 프레임 처리 수 (FunctionCounter)
        FunctionCounter.builder(metricName, this.stats, s -> {
                    StompSubProtocolHandler.Stats stompStats = s.getStompSubProtocolStats();
                    return (stompStats != null) ? stompStats.getTotalConnected() : 0;
                })
                .description("Total number of STOMP CONNECTED frames sent")
                .tag("action", "CONNECTED")
                .register(registry);

        // DISCONNECT 프레임 처리 수 (FunctionCounter)
        FunctionCounter.builder(metricName, this.stats, s -> {
                    StompSubProtocolHandler.Stats stompStats = s.getStompSubProtocolStats();
                    return (stompStats != null) ? stompStats.getTotalDisconnect() : 0;
                })
                .description("Total number of STOMP DISCONNECT frames processed")
                .tag("action", "DISCONNECT")
                .register(registry);
    }

    // --- 3. 채널 Executor 큐 통계 등록 ---
    private void registerChannelExecutorMetrics() {
        // 이미 Micrometer Actuator가 executor_queued_tasks 등으로 등록할 가능성이 높지만,
        // 명시적인 이름으로 재등록하여 보장성을 높입니다.

        // 인바운드 채널 대기 큐 크기 (Gauge)
        registry.gauge("websocket.channel.queue.size", Collections.singletonList(Tag.of("channel", "inbound")),
                this.stats, s -> {
                    String info = s.getClientInboundExecutorStatsInfo();
                    return extractQueuedTasks(info);
                });

        // 아웃바운드 채널 대기 큐 크기 (Gauge)
        registry.gauge("websocket.channel.queue.size", Collections.singletonList(Tag.of("channel", "outbound")),
                this.stats, s -> {
                    String info = s.getClientOutboundExecutorStatsInfo();
                    return extractQueuedTasks(info);
                });
    }

    // ThreadPoolExecutor 문자열에서 queued tasks 값을 추출하는 헬퍼 메서드
    private double extractQueuedTasks(String statsInfo) {
        if (statsInfo.contains("queued tasks = ")) {
            try {
                int start = statsInfo.indexOf("queued tasks = ") + "queued tasks = ".length();
                int end = statsInfo.indexOf(",", start);
                if (end == -1) {
                    end = statsInfo.indexOf("]", start);
                }
                String value = statsInfo.substring(start, end).trim();
                return Double.parseDouble(value);
            } catch (Exception e) {
                // 파싱 오류 시 0 반환
                return 0;
            }
        }
        return 0;
    }
}
