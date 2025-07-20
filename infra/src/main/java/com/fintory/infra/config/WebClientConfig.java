package com.fintory.infra.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${hantu-openapi.appkey}")
    private String appkey;

    @Value("${hantu-openapi.appsecret}")
    private String appsecret;

    @Bean
    @Qualifier("kisTokenWebClient")
    public WebClient kisTokenWebClient(){
        return WebClient.builder()
                //.baseUrl("https://openapivts.koreainvestment.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    @Qualifier("kisStockInfoWebClient")
    public WebClient kisStockInfoWebClient(){
        return WebClient.builder()
                .baseUrl("https://openapi.koreainvestment.com:9443")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT_CHARSET, "utf-8")
                .defaultHeader("appkey",appkey)
                .defaultHeader("appsecret",appsecret)
                .defaultHeader("custtype","P")
                .defaultHeader("tr_id","CTPF1002R")
                .build();
    }

    @Bean
    @Qualifier("kisStockInfoOverseasWebClient")
    public WebClient kisStockInfoOverseasWebClient(){
        return WebClient.builder()
                .baseUrl("https://openapi.koreainvestment.com:9443")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT_CHARSET, "utf-8")
                .defaultHeader("appkey",appkey)
                .defaultHeader("appsecret",appsecret)
                .defaultHeader("custtype","P")
                .defaultHeader("tr_id","CTPF1702R")
                .build();
    }


    @Bean
    @Qualifier("kisItemChartPriceWebClient")
    public WebClient kisItemChartPriceWebClient(){
        return WebClient.builder()
                .baseUrl("https://openapi.koreainvestment.com:9443")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT_CHARSET, "utf-8")
                .defaultHeader("appkey",appkey)
                .defaultHeader("appsecret",appsecret)
                .defaultHeader("custtype","P")
                .defaultHeader("tr_id","FHKST03010100")

                .build();
    }


    @Bean
    @Qualifier("kisItemChartPriceOverseasWebClient")
    public WebClient kisItemChartPriceOverseasWebClient(){
        return WebClient.builder()
                .baseUrl("https://openapi.koreainvestment.com:9443")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT_CHARSET, "utf-8")
                .defaultHeader("appkey",appkey)
                .defaultHeader("appsecret",appsecret)
                .defaultHeader("custtype","P")
                .defaultHeader("tr_id","FHKST03030100")
                .build();
    }


    @Bean
    @Qualifier("kisPriceQuoteWebClient")
    public WebClient kisPriceQuoteWebClient(){
        return WebClient.builder()
                .baseUrl("https://openapivts.koreainvestment.com:29443")
                .defaultHeader(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT_CHARSET,"utf-8")
                .defaultHeader("appkey",appkey)
                .defaultHeader("appsecret",appsecret)
                .defaultHeader("tr_id","FHKST01010200")
                .defaultHeader("custtype","P")
                .build();
    }

    @Bean
    @Qualifier("kisRealPriceWebClient")
    public WebClient kisRealPriceWebClient(){
        return WebClient.builder()
                .baseUrl("https://openapi.koreainvestment.com:9443")
                .defaultHeader(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT_CHARSET,"utf-8")
                .defaultHeader("appkey",appkey)
                .defaultHeader("appsecret",appsecret)
                .defaultHeader("tr_id","FHKST01010100")
                .defaultHeader("custtype","P")
                .build();
    }

    @Bean
    @Qualifier("kisRealPriceOverseasWebClient")
    public WebClient kisRealPriceOverseasWebClient(){
        return WebClient.builder()
                .baseUrl("https://openapi.koreainvestment.com:9443")
                .defaultHeader(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT_CHARSET,"utf-8")
                .defaultHeader("appkey",appkey)
                .defaultHeader("appsecret",appsecret)
                .defaultHeader("tr_id","HHDFS76200200")
                .defaultHeader("custtype","P")
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
