package com.fintory.auth.service;


import com.fintory.auth.dto.AuthToken;
import com.fintory.auth.jwt.JwtTokenProvider;
import com.fintory.auth.util.CustomUserDetails;
import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.child.model.Child;
import com.fintory.domain.child.model.LoginType;
import com.fintory.domain.child.model.Status;
import com.fintory.domain.common.Role;
import com.fintory.infra.domain.child.repository.ChildRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOauthService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    private final JwtTokenProvider jwtTokenProvider;
    private final ChildRepository childRepository;
    private final RedisTemplate<String, String> redisTemplate;


    @Transactional
    public AuthToken handleGoogleLoginOrRegister(String idToken) {
        GoogleIdToken.Payload payload = verifyGoogleToken(idToken);

        String nickname = (String) payload.get("name");
        String googleEmail = payload.getEmail();
        String socialId = payload.getSubject();

        //idpw로 가입되어있는지 검사
        Optional<Child> check = childRepository.findByEmail(googleEmail);
        if (check.isPresent() && check.get().getLoginType() != LoginType.GOOGLE) {
            throw new DomainException(DomainErrorCode.ALREADY_REGISTERED_EMAIL);
        }

        Child child = childRepository.findBySocialId(socialId) // 소셜로만 가입되있는 경우 -> 로그인
                .orElseGet(() -> { // 소셜로그인 가입 안되어있는 경우 -> 등록 후 로그인
                    Child newChild = new Child(nickname, googleEmail, socialId, LoginType.GOOGLE, Role.CHILD, Status.ACTIVE);
                    return childRepository.save(newChild);
                });
        // 인증 객체 생성 (비밀번호 없이)
        CustomUserDetails userDetails = new CustomUserDetails(
                child.getSocialId(),  // username
                null,                 // password: 소셜 로그인은 비밀번호 불필요
                child.getNickname(),
                child.getRole().getKey(),
                child.getLoginType()
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication.getName());

        log.info("authentication.getName(): {}", authentication.getName());
        redisTemplate.opsForValue().set(
                authentication.getName(),
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpirationDays() * 24 * 60 * 60 * 1000L,
                TimeUnit.MILLISECONDS
        );
        log.info("at: {}, rt: {}", accessToken, refreshToken);
        return new AuthToken(accessToken, refreshToken);
    }

    public GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        try {
            NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                return idToken.getPayload();
            } else {
                throw new DomainException(DomainErrorCode.INVALID_TOKEN);
            }
        } catch (Exception e) {
            throw new RuntimeException("구글 토큰 검증 및 추출 실패", e);
        }
    }
}
