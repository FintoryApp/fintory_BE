package com.fintory.infra.domain.stock.service.token;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.token.DBTokenResponse;
import com.fintory.domain.stock.service.token.DBTokenIssueService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class DBTokenIssueServiceImpl implements DBTokenIssueService {

    private final RestTemplate restTemplate;
    private final RedisTemplate<Object, Object> redisTemplate;

    @Value("${db-openapi.base-url}")
    private String baseUrl;

    @Value("${db-openapi.db-appkey}")
    private String dbAppKey;

    @Value("${db-openapi.db-appsecret}")
    private String dbAppSecret;

    @PostConstruct
    public void refreshDBToken() {
        try {
            DBTokenResponse token = getNewDBToken();
            log.info("DB 토큰 초기화 성공");
        } catch (Exception e) {
            log.error("DB 토큰 초기화 실패: {}", e.getMessage());
            throw new DomainException(DomainErrorCode.DB_TOKEN_ISSUE_ERROR);
        }
    }

    // 24시간마다 토큰 갱신
    @Scheduled(fixedRate = 86400000, initialDelay = 86400000)
    public void changeDBToken() {
        try {
            getNewDBToken();
            log.info("DB 토큰 갱신 완료");
        } catch (Exception e) {
            log.error("DB 토큰 갱신 실패: {}", e.getMessage());
        }
    }

    // DB 증권 접근 토큰 발급
    @Override
    public DBTokenResponse getNewDBToken() {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .path("/oauth2/token")
                    .queryParam("grant_type", "client_credentials")
                    .queryParam("appkey", dbAppKey)
                    .queryParam("appsecretkey", dbAppSecret)
                    .queryParam("scope", "oob")
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<DBTokenResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, DBTokenResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                storeTokenWithExpiry(response.getBody());
                return response.getBody();
            } else {
                throw new DomainException(DomainErrorCode.DB_TOKEN_ISSUE_ERROR);
            }

        } catch (Exception e) {
            log.error("DB 토큰 발급 실패: {}", e.getMessage());
            throw new DomainException(DomainErrorCode.DB_TOKEN_ISSUE_ERROR);
        }
    }

    // DB 증권 접근 토큰 Redis에 저장
    private void storeTokenWithExpiry(DBTokenResponse response) {
        String token = response.token();

        // 24시간에서 5분 여유를 뺀 시간
        Duration expiry = Duration.ofHours(24).minusMinutes(5);
        redisTemplate.opsForValue().set("db-access-token", token, expiry);
    }
}