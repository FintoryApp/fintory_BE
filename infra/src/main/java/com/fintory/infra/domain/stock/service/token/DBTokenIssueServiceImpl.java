package com.fintory.infra.domain.stock.service.token;

import com.fintory.domain.stock.dto.token.DBTokenResponse;
import com.fintory.domain.stock.service.token.DBTokenIssueService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Slf4j
@Service
public class DBTokenIssueServiceImpl implements DBTokenIssueService {

    @Value("${db-openapi.db-appkey}")
    private String dbAppKey;

    @Value("${db-openapi.db-appsecret}")
    private String dbAppSecret;

    @Autowired
    @Qualifier("dbWebClient")
    private  WebClient dbWebClient;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @PostConstruct
    public void refreshDBToken(){
        getNewDBToken()
                .doOnSuccess(dbTokenResponse -> {
                    log.info("DB 토큰 발급 완료");
                })
                .doOnError(error->{
                    log.info("DB 토큰 발급 오류:{}",error.getMessage());
                })
                .subscribe();
    }

    //24시간 마다 토큰 갱신
    @Scheduled(fixedRate = 86400000, initialDelay =  82800000)
    public void changeDBToken(){
        getNewDBToken()
                .doOnSuccess(dbTokenResponse -> {
                    log.info("DB 토큰 발급 완료");
                })
                .doOnError(error->{
                    log.info("DB 토큰 발급 오류:{}",error.getMessage());
                })
                .subscribe();
    }

    // DB 증권 접근 토큰 발급
    @Override
    public Mono<DBTokenResponse> getNewDBToken(){
        return dbWebClient.post()
                .uri(uriBuilder->uriBuilder
                        .path("/oauth2/token")
                        .queryParam("grant_type","client_credentials")
                        .queryParam("appkey",dbAppKey)
                        .queryParam("appsecretkey",dbAppSecret)
                        .queryParam("scope","oob")
                        .build()
                )
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .retrieve()
                .bodyToMono(DBTokenResponse.class)
                .doOnSuccess(this::storeTokenWithExpiry);
    }

    // DB 증권 접근 토큰 Redis에 저장
    private void storeTokenWithExpiry(DBTokenResponse response){
        String token = response.token();
        redisTemplate.opsForValue().set("db-access-token",token);
    }
}
