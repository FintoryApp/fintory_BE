package com.fintory.infra.domain.stock.service.token;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.token.KisTokenRequest;
import com.fintory.domain.stock.dto.token.KisTokenResponse;
import com.fintory.domain.stock.dto.token.KisWebSocketTokenRequest;
import com.fintory.domain.stock.dto.token.KisWebSocketTokenResponse;
import com.fintory.domain.stock.service.token.KisTokenIssueService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.Duration;
import org.springframework.data.redis.core.RedisTemplate;


@Service
@Slf4j
@RequiredArgsConstructor
public class KisTokenIssueServiceImpl implements KisTokenIssueService {

    @Autowired
    @Qualifier("kisWebClient")
    private final WebClient kisWebClient;

    @Value("${hantu-openapi.appkey}")
    private String appKey;

    @Value("${hantu-openapi.appsecret}")
    private String secretkey;

    @Autowired
    private  RedisTemplate<Object, Object> redisTemplate;


    @PostConstruct
    public void refreshKisToken(){
        // REST API 토큰
        getNewKisToken()
                .doOnSuccess(token -> {
                    log.info("REST API 토큰 발급 성공");
                })
                .doOnError(error -> {
                    error.printStackTrace();
                    log.error("KIS REST 토큰 발급 실패: {}", error.getMessage());
                    throw new DomainException(DomainErrorCode.KIS_TOKEN_ISSUE_ERROR);
                })
                .subscribe();

        // WebSocket 토큰
        getNewWebSocketKisToken()
                .doOnSuccess(token -> {
                    log.info("WebSocket 토큰 발급 성공");
                })
                .doOnError(error -> {
                    error.printStackTrace();
                    log.error("KIS WebSocket 토큰 발급 실패: {}", error.getMessage());
                    throw new DomainException(DomainErrorCode.KIS_WEBSOCKET_TOKEN_ISSUE_ERROR);
                })
                .subscribe();
    }

    //24시간 마다 토큰 갱신
    @Scheduled(fixedRate = 86400000, initialDelay =  86400000)
    public void changeRefreshToken(){

        getNewKisToken()
                .doOnSuccess(token -> log.info("REST API 토큰 갱신 완료"))
                .doOnError(error -> {
                    log.error("REST API 토큰 갱신 실패: {}", error.getMessage());
                    throw new DomainException(DomainErrorCode.KIS_TOKEN_ISSUE_ERROR);
                })
                .subscribe();

        getNewWebSocketKisToken()
                .doOnSuccess(token -> log.info("WebSocket 토큰 갱신 완료"))
                .doOnError(error -> {
                    log.error("WebSocket 토큰 갱신 실패: {}", error.getMessage());
                    throw new DomainException(DomainErrorCode.KIS_WEBSOCKET_TOKEN_ISSUE_ERROR);
                })
                .subscribe();
    }


    //일반 접근 토큰 발급
    @Override
    public Mono<KisTokenResponse> getNewKisToken(){
        return kisWebClient.post()
                .uri("/oauth2/tokenP")
                .bodyValue(createKisRequestToken())
                .retrieve()
                .bodyToMono(KisTokenResponse.class)
                .doOnSuccess(this::storeTokenWithExpiry);
    }

    // 웹소켓 접속 토큰 발급
    @Override
    public Mono<KisWebSocketTokenResponse> getNewWebSocketKisToken(){
        return kisWebClient.post()
                .uri("/oauth2/Approval")
                .bodyValue(createKisWebSocketRequestToken())
                .retrieve()
                .bodyToMono(KisWebSocketTokenResponse.class)
                .doOnSuccess(this::storeWebSocketTokenWithExpiry);
    }

    // 일반 접근 토큰 Redis 저장
    private void storeTokenWithExpiry(KisTokenResponse response){
        String token = response.accessToken();
        int expiresIn = response.expiresIn();

        // Redis 만료 설정
        Duration expiry = Duration.ofSeconds(expiresIn-300);
        redisTemplate.opsForValue().set("kis-access-token", token, expiry);

    }

    //웹소켓 접속 토큰 Redis에 저장
    private void storeWebSocketTokenWithExpiry(KisWebSocketTokenResponse response){
        String token = response.approvalKey();
        redisTemplate.opsForValue().set("kis-websocket-access-token", token);
    }


    // 일반 접근 토큰 요청 객체 생성
    private KisTokenRequest createKisRequestToken(){
        return new KisTokenRequest(
                appKey,
                secretkey,
                "client_credentials"
        );
    }

    //웹소켓 접속 토큰 요청 객체 생성
    private KisWebSocketTokenRequest createKisWebSocketRequestToken(){
        return new KisWebSocketTokenRequest(
                appKey,
                secretkey,
                "client_credentials"
        );
    }

}
