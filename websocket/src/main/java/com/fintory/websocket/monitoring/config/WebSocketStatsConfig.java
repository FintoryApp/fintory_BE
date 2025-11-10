package com.fintory.websocket.monitoring.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;

@Configuration
public class WebSocketStatsConfig {

    // 설정 파일의 속성 값을 읽어와 주입
    @Value("${spring.websocket.stomp.stats-log-period:30000}")
    private long loggingPeriodMillis;

    private final WebSocketMessageBrokerStats stats;

    public WebSocketStatsConfig(WebSocketMessageBrokerStats stats) {
        this.stats = stats;
    }

    @PostConstruct
    public void setCustomLoggingPeriod() {
        if (this.loggingPeriodMillis > 0) {
            this.stats.setLoggingPeriod(this.loggingPeriodMillis);
        }
    }
}
