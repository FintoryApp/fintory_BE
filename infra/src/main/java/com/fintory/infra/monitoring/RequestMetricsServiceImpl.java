package com.fintory.infra.monitoring;

import com.fintory.domain.common.service.RequestMetricsService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Service;

@Service
public class RequestMetricsServiceImpl implements RequestMetricsService {

    private final MeterRegistry registry;
    private static final String METRIC_NAME = "requests.custom.total";

    public RequestMetricsServiceImpl(MeterRegistry registry) {
        this.registry = registry;
    }

    // 특정 api 요청이 발생했을 때 카운터 증가
    public void incrementRequestCounter(String method, String api) {
        Tags tags = Tags.of("method", method, "api", api);
        registry.counter(METRIC_NAME, tags)
                .increment();
    }
}
