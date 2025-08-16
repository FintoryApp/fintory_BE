package com.fintory.infra.domain.stock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpClientConfig {

    /**
     *
     *TODO 초기에는 API 호출과 DB 저장 작업을 비동기로 처리하려 했으나,
     * 스레드 간 전환 시 트랜잭션 컨텍스트 유실과 데이터 원자성 보장 문제로 인해 동기 방식으로 변경.
     * "데이터 가져오기 + DB 저장"을 하나의 원자적 작업 단위로 보장하기 위해 같은 스레드에서 순차 처리함.
     */
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
