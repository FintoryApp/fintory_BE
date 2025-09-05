package com.fintory.infra.domain.stock.config;


import com.google.common.net.HttpHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WebClientConfig {

    /**
     * KIS DEVELOPER와 통신하기 위한 전용 WebClient
     * */
    @Bean
    @Qualifier("kisWebClient")
    public WebClient kisWebClient() {
        return WebClient.builder()
                .baseUrl("https://openapi.koreainvestment.com:9443")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    @Qualifier("dbWebClient")
    public WebClient dbWebClient() {
        return WebClient.builder()
                .baseUrl("https://openapi.dbsec.co.kr:8443")
                .build();
    }

    @Bean
    @Qualifier("yahooWebClient")
    public WebClient yahooWebClient(){
        return WebClient.builder()
                .baseUrl("https://query1.finance.yahoo.com")
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();
    }

}