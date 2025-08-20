package com.fintory.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fintory.auth.dto.AuthToken;
import com.fintory.auth.dto.KakaoUserInfo;
import com.fintory.auth.jwt.JwtTokenProvider;
import com.fintory.auth.util.CustomUserDetails;
import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.child.model.Child;
import com.fintory.domain.child.model.LoginType;
import com.fintory.domain.child.model.Status;
import com.fintory.domain.common.Role;
import com.fintory.infra.domain.child.repository.ChildRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOauthService {

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String userinfoUri;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ChildRepository childRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final HttpHeaders headers = new HttpHeaders();

    @Transactional
    public AuthToken handleKakaoLoginOrRegister(String kakaoAccessToken) {

        // access token 으로 사용자 정보 요청
        KakaoUserInfo userInfo = getKakaoUserInfo(kakaoAccessToken);
        String kakaoId = userInfo.id();
        String nickname = userInfo.nickname();
        String kakaoEmail = userInfo.email();
        log.info("kakaoId: {}, nickname: {}, email: {}", kakaoId, nickname, kakaoEmail);

        // id, pw로 가입되어있는지 검사
        Optional<Child> check = childRepository.findByEmail(kakaoEmail);
        if (check.isPresent() && check.get().getLoginType() != LoginType.KAKAO) {
            throw new DomainException(DomainErrorCode.ALREADY_REGISTERED_EMAIL);
        }

        Child child = childRepository.findBySocialId(kakaoId)
                .orElseGet(() -> {
                    Child newChild = new Child(nickname, kakaoEmail, kakaoId, LoginType.KAKAO, Role.CHILD, Status.ACTIVE);
                    return childRepository.save(newChild);
                });

        CustomUserDetails userDetails = new CustomUserDetails(
                child.getSocialId(),
                null,
                child.getNickname(),
                child.getRole().getKey(),
                child.getLoginType()
        );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication.getName());

        redisTemplate.opsForValue().set(
                authentication.getName(),
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpirationDays() * 24 * 60 * 60 * 1000L,
                TimeUnit.MILLISECONDS
        );

        log.info("authentication.getName(): {}", authentication.getName());
        log.info("at: {}, rt: {}", accessToken, refreshToken);
        return new AuthToken(accessToken, refreshToken);
    }

    private KakaoUserInfo getKakaoUserInfo(String accessToken) {

        headers.set("Authorization", "Bearer " + accessToken);
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        ResponseEntity<JsonNode> response = restTemplate.exchange(
            userinfoUri,
            HttpMethod.GET,
            entity,
            JsonNode.class
        );
        
        JsonNode body = response.getBody();

        if (body == null) {
            throw new RuntimeException("카카오 서버에서 응답을 받지 못했습니다.");
        }
        
        String id = body.path("id").asText();
        if (id.isEmpty()) {
            throw new RuntimeException("카카오 사용자 ID를 받지 못했습니다.");
        }
        
        JsonNode kakaoAccount = body.path("kakao_account");
        String nickname = kakaoAccount.path("profile")
                                .path("nickname")
                                .asText("");
        
        String email = kakaoAccount.path("email").asText("");
        if (email.isEmpty()) {
            throw new RuntimeException("카카오 계정 이메일을 받지 못했습니다.");
        }
        
        return new KakaoUserInfo(id, nickname, email);
    }

}