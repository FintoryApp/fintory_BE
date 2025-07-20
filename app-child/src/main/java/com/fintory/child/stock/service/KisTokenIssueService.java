package com.fintory.child.stock.service;

import com.fintory.child.stock.dto.KisTokenRequest;
import com.fintory.child.stock.dto.KisTokenResponse;
import com.fintory.child.stock.dto.KisWebSocketTokenRequest;
import com.fintory.child.stock.dto.KisWebSocketTokenResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class KisTokenIssueService {

    @Value("${hantu-openapi.appkey}")
    private String appkey;

    @Value("${hantu-openapi.appsecret}")
    private String appsecret;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    @Qualifier("kisTokenWebClient")
    private WebClient kisWebClient;

    @PostConstruct
    public void initializeToken(){
        refreshKisToken();
        refreshKisWebSocketToken();

    }

    //@Scheduled(fixedRate =21600000)
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

    //@Scheduled(fixedRate= 82800000)
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
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createKisRequestToken())
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    return response.bodyToMono(String.class)
                            .doOnNext(errorBody -> {
                                System.out.println("HTTP Status: " + response.statusCode());
                                System.out.println("Response Headers: " + response.headers().asHttpHeaders());
                                System.out.println("Error Body: " + errorBody);
                                System.out.println("=================================");
                            })
                            .then(Mono.error(new RuntimeException("KIS API 호출 실패: " )));
                })
                .bodyToMono(KisTokenResponse.class)
                .map(KisTokenResponse::getAccessToken);
    }

    private Mono<String> getNewKisWebSocketToken(){
        return kisWebClient.post()
                .uri("https://openapi.koreainvestment.com:9443/oauth2/Approval")
                .bodyValue(createKisRequestWebsocketToken())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    return response.bodyToMono(String.class)
                            .doOnNext(errorBody -> {
                                System.out.println("HTTP Status: " + response.statusCode());
                                System.out.println("Response Headers: " + response.headers().asHttpHeaders());
                                System.out.println("Error Body: " + errorBody);
                                System.out.println("=================================");
                            })
                            .then(Mono.error(new RuntimeException("KIS API 호출 실패: " )));
                })
                .bodyToMono(KisWebSocketTokenResponse.class)
                .map(KisWebSocketTokenResponse::getApprovalKey);
    }

    private KisTokenRequest createKisRequestToken(){
        return KisTokenRequest.builder()
                .appkey(appkey)
                .appsecret(appsecret)
                .grantType("client_credentials")
                .build();
    }

    private KisWebSocketTokenRequest createKisRequestWebsocketToken(){
        return KisWebSocketTokenRequest.builder()
                .appkey(appkey)
                .secretkey(appsecret)
                .grantType("client_credentials")
                .build();
    }


}
