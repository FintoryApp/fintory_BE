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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class KisTokenIssueServiceImpl implements KisTokenIssueService {

    private final RestTemplate restTemplate;
    private final RedisTemplate<Object, Object> redisTemplate;

    @Value("${hantu-openapi.base-url}")
    private String baseUrl;

    @Value("${hantu-openapi.appkey}")
    private String appKey;

    @Value("${hantu-openapi.appsecret}")
    private String secretkey;

    @PostConstruct
    public void refreshKisToken() {
        try {
            // REST API 토큰 발급
            KisTokenResponse restToken = getNewKisToken();
            log.info("REST API 토큰 발급 성공");

            // WebSocket 토큰 발급
            KisWebSocketTokenResponse wsToken = getNewWebSocketKisToken();
            log.info("WebSocket 토큰 발급 성공");

        } catch (Exception e) {
            log.error("토큰 초기화 실패: {}", e.getMessage());
            throw new DomainException(DomainErrorCode.KIS_TOKEN_ISSUE_ERROR);
        }
    }

    // 24시간마다 토큰 갱신
    @Scheduled(fixedRate = 86400000, initialDelay = 86400000)
    public void changeRefreshToken() {
        try {
            // REST API 토큰 갱신
            getNewKisToken();
            log.info("REST API 토큰 갱신 완료");

            // WebSocket 토큰 갱신
            getNewWebSocketKisToken();
            log.info("WebSocket 토큰 갱신 완료");

        } catch (Exception e) {
            log.error("토큰 갱신 실패: {}", e.getMessage());
        }
    }

    // 일반 접근 토큰 발급
    @Override
    public KisTokenResponse getNewKisToken() {
        try {
            String url = baseUrl + "/oauth2/tokenP";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<KisTokenRequest> entity = new HttpEntity<>(createKisRequestToken(), headers);

            ResponseEntity<KisTokenResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, KisTokenResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                storeTokenWithExpiry(response.getBody());
                return response.getBody();
            } else {
                throw new DomainException(DomainErrorCode.KIS_TOKEN_ISSUE_ERROR);
            }

        } catch (Exception e) {
            log.error("KIS REST 토큰 발급 실패: {}", e.getMessage());
            throw new DomainException(DomainErrorCode.KIS_TOKEN_ISSUE_ERROR);
        }
    }

    // 웹소켓 접속 토큰 발급
    @Override
    public KisWebSocketTokenResponse getNewWebSocketKisToken() {
        try {
            String url = baseUrl + "/oauth2/Approval";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<KisWebSocketTokenRequest> entity = new HttpEntity<>(createKisWebSocketRequestToken(), headers);

            ResponseEntity<KisWebSocketTokenResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, KisWebSocketTokenResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                storeWebSocketTokenWithExpiry(response.getBody());
                return response.getBody();
            } else {
                throw new DomainException(DomainErrorCode.KIS_WEBSOCKET_TOKEN_ISSUE_ERROR);
            }

        } catch (Exception e) {
            log.error("KIS WebSocket 토큰 발급 실패: {}", e.getMessage());
            throw new DomainException(DomainErrorCode.KIS_WEBSOCKET_TOKEN_ISSUE_ERROR);
        }
    }

    // 일반 접근 토큰 Redis 저장
    private void storeTokenWithExpiry(KisTokenResponse response) {
        String token = response.accessToken();
        int expiresIn = response.expiresIn();

        // Redis 만료 설정 (5분 여유)
        Duration expiry = Duration.ofSeconds(expiresIn - 300);
        redisTemplate.opsForValue().set("kis-access-token", token, expiry);
    }

    // 웹소켓 접속 토큰 Redis에 저장
    private void storeWebSocketTokenWithExpiry(KisWebSocketTokenResponse response) {
        String token = response.approvalKey();

        //Redis 만료 설정 (5분 여유)
        Duration expiry = Duration.ofSeconds(86100);
        redisTemplate.opsForValue().set("kis-websocket-access-token", token);
    }

    // 일반 접근 토큰 요청 객체 생성
    private KisTokenRequest createKisRequestToken() {
        return new KisTokenRequest(
                appKey,
                secretkey,
                "client_credentials"
        );
    }

    // 웹소켓 접속 토큰 요청 객체 생성
    private KisWebSocketTokenRequest createKisWebSocketRequestToken() {
        return new KisWebSocketTokenRequest(
                appKey,
                secretkey,
                "client_credentials"
        );
    }
}