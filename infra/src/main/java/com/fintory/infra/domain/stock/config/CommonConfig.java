package com.fintory.infra.domain.stock.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CommonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    //TODO 좀 더 자세한 설정이 필요한 경우 변경하기
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
