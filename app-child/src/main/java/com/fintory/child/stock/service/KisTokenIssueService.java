package com.fintory.child.stock.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KisTokenIssueService {

    @Value("${hantu-openapi.appkey}")
    private String appKey;

    @Value("${hantu-openapi.appsecret}")
    private String appSecret;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    @Qualifier
    private WebClient kisWebClient;

    @PostConstruct
    public void initializeToken(){
        refreshKisToken();
        refreshKisWebSocketToken();

    }

    @Scheduled(fixedRate =21600000)
    public void refreshKisToken(){
        getNewKisToken()
                .doOnSuccess(token-> {
                    redisTemplate.opsForValue().set("kis-access-token",token);
                })
                .doOnError(error->{
                    log.error("KIS 토큰 갱신 실패: {} ", error.getMessage());
                })
                .subscribe();
    }

    @Scheduled(fixedRate= 82800000)
    public void refreshKisWebSocketToken(){
        getNewKisWebSocketToken()
                .doOnSuccess(token->{
                    redisTemplate.opsForValue().set("kis-websocket-token",token);

                })
                .doOnError(error->{
                    log.error("KSI 토큰 갱신 실패: {} ", error.getMessage());
                })
                .subscribe();
    }

    private Mono<String> getNewKisToken(){
        return kisWebClient.post()
                .uri("https://openapi.koreainvestment.com:9443/oauth2/tokenP")
                .bodyValue(createKisRequestToken())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(KisTokenResponse.class)
                .map(KisTokenResponse::getAccessToken);
    }

    private Mono<String> getNewKisWebSocketToken(){
        return kisWebClient.post()
                .uri("https://openapi.koreainvestment.com:9443:9443//oauth2/Approval")
                .bodyValue(createKisRequestToken())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(KisWebSocketTokenResposne.class)
                .map(KisWebSocketTokenResponse::getAccessToken);
    }

    private KisTokenRequest createKisRequestToken(){
        return KisTokenRequest.builder()
                .appkey(appkey)
                .appsecret(appsecret)
                .build();
    }


}
