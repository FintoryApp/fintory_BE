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
    // 여러 스트림(종목)을 위한 채널 풀 정의
    private final Map<String, Sinks.Many<String>> sinks = new ConcurrentHashMap<>();

    // 채널에 실시간 데이터 발행
    public void publish(String json) {
        try{
            Tick tick = objectMapper.readValue(json, Tick.class);
            createAndPublishSinks(tick.code()).tryEmitNext(json); // 채널 만들고, 원본json을 채널로 발행
        } catch (Exception e) {
            log.error("Error while publishing stock realtime", e);
        }
    }


    // 기존에 채널이 없으면 생성하고 sink 리턴. 있으면 sink만 리턴.
    private Sinks.Many<String> createAndPublishSinks(String code) {
        return sinks.computeIfAbsent(code, k -> Sinks.many()
                .multicast()
                .onBackpressureBuffer(1, false) // 백프레셔로 drop을 택했으니 버퍼가 클 필요 없음 + 구취해도 sink 살아있음 -> 재사용
        );
    }

    // 클라이언트가 채널을 구독
    public Flux<String> subscribe(String code) {
        return subscribeSinks(code) //채널 가져오기
                .asFlux()
                .onBackpressureDrop(); // 클라이언트 속도가 느려서 못가져가는거면 그냥 버리고 새 값만 취급
    }

    /*
        구독할 sink를 리턴.
     */
    private Sinks.Many<String> subscribeSinks(String code){
        return sinks.compute(code, (key, currentSink) -> currentSink);
    }
}
