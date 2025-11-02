package com.fintory.domain.common.service;

public interface RequestMetricsService {

    void incrementRequestCounter(String method, String api);

}
