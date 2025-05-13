package com.fintory.fintory.backend.project.consulting.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;


@Configuration
public class ChatGptConfig {

    @Value("${chatgpt.api-key}")
    private String secretKey;

    //외부 api 서버와의 http 통신을 위한 RestTemplate Bean 등록 -> WebClient로 대체 가능.
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate;
    }

    //외부 API 요청 시 사용할 공통 HttpHeaders Bean
    @Bean
    public HttpHeaders httpHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(secretKey); //Authorization Bearer <secretKey>
        headers.setContentType(MediaType.APPLICATION_JSON); //Content-Type : application/json
        return headers;
    }
}
