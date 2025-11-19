package com.fintory.websocketwebflux.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintory.websocketwebflux.dto.Tick;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
    1. 브로드캐스팅 채널 생성
    2. 발행
    3. 구독
 */
@Slf4j
@Service
public class StockRealtimeService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    // 여러 스트림을 위한 채널 풀 정의
    private final Map<String, Sinks.Many<String>> sinks = new ConcurrentHashMap<>();


    // 채널에 실시간 데이터 발행
    public void publish(String json) {
        try{
            Tick tick = objectMapper.readValue(json, Tick.class);
            getOrCreateSink(tick.code()).tryEmitNext(json); // 채널 만들고, 원본json을 채널로 발행
        } catch (Exception e) {
            log.error("Error while publishing stock realtime", e);
        }
    }

    // 클라이언트가 채널을 구독
    public Flux<String> stream(String code) {
        return getOrCreateSink(code).asFlux();
    }

    // 특정 종목으로 채널 생성 혹은 채널 가져오기
    private Sinks.Many<String> getOrCreateSink(String code) {
        return sinks.computeIfAbsent(code, k -> Sinks.many()
                .multicast()
                .onBackpressureBuffer(1)
        );
    }

}
