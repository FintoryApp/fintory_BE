package com.fintory.auth.service;

import com.fintory.auth.dto.AuthToken;
import com.fintory.auth.dto.request.SignUpRequest;
import com.fintory.auth.jwt.JwtTokenProvider;
import com.fintory.auth.util.CustomUserDetails;
import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.child.model.Child;
import com.fintory.domain.child.model.LoginType;
import com.fintory.domain.child.model.Status;
import com.fintory.domain.common.Role;
import com.fintory.infra.domain.child.repository.ChildRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 로그인, 토큰 재발급 등 인증 관련 비즈니스 로직을 담당하는 서비스 클래스.
 * JwtTokenProvider와 Redis를 활용하여 토큰을 관리합니다.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    // 남은 시간 2일 이하라면 RT 갱신
    private static final long REFRESH_THRESHOLD_MS = 2L * 24 * 60 * 60 * 1000;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final ChildRepository childRepository;

    @Transactional
    public AuthToken signup(SignUpRequest request) {

        // 이메일 중복 최종 검증: 이메일 중복 api를 통해 사용자는 이미 1차 검증을 하지만,
        // 회원가입 도중에 다른 사용자가 같은 이메일로 가입을 완료했다면 최종적으로 중복이 됨
        if (checkDuplicateEmail(request.email())) {
            throw new DomainException(DomainErrorCode.ALREADY_REGISTERED_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        Child child = Child.idPwBuilder()
                .email(request.email())
                .password(encodedPassword)
                .nickname(request.nickname())
                .role(Role.CHILD)
                .status(Status.ACTIVE)
                .loginType(LoginType.EMAIL)
                .build();

        childRepository.save(child);

        // 회원가입 완료 후 자동으로 로그인 처리
        return login(request.email(), request.password());
    }

    /**
     * 이메일 중복 확인 로직. true면 중복
     */
    @Transactional(readOnly = true)
    public boolean checkDuplicateEmail(String email) {

        return childRepository.findByEmail(email).isPresent();
    }

    public AuthToken login(String email, String password) {
        try {
            // 이메일, 비밀번호 기반 인증 토큰 생성
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
            // 인증 수행 (내부적으로 UserDetailsService 호출)
            //authenticate()가 내부적으로 CustomUserService.loadUserByUsername 호출하여 userDetails 반환
            //userDetails를 포함한 모든 인증 정보를 Authentication에 담음.
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails customUserDetails) {
                log.info("인증 직후 password 값: {}", customUserDetails.getPassword());
            }
            // 인증 성공 시 SecurityContext에 등록
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("authentication: {}", authentication);

            // 액세스/리프레시 토큰 발급
            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication.getName());
            long refreshTokenExpirationMillis =
                    jwtTokenProvider.getRefreshTokenExpirationDays() * 24 * 60 * 60 * 1000L;

            // 리프레시 토큰 Redis 저장
            redisTemplate.opsForValue().set(
                    authentication.getName(),
                    refreshToken,
                    refreshTokenExpirationMillis,
                    TimeUnit.MILLISECONDS
            );
            log.info("at: {}, rt: {}", accessToken, refreshToken);

            return new AuthToken(accessToken, refreshToken);

        } catch (BadCredentialsException e) {
            throw new DomainException(DomainErrorCode.WRONG_EMAIL_OR_PASSWORD);
        }
    }

    /**
     * 토큰 재발급 로직
     */
    public AuthToken reissue(String refreshToken) {

        // Refresh Token 유효성 검증
        jwtTokenProvider.validateToken(refreshToken);

        // Refresh Token 에서 사용자 정보(subject) 추출
        Claims claims = jwtTokenProvider.parseClaims(refreshToken);
        String userIdentifier = claims.getSubject();
        log.info("userIdentifier: {}", userIdentifier);

        // 카테고리 검사 (rt가 맞는지)
        if (!"refresh".equals(jwtTokenProvider.getTokenCategory(refreshToken))) {
            throw new DomainException(DomainErrorCode.INVALID_TOKEN_TYPE); // or custom: INVALID_TOKEN_TYPE
        }

        // 요청한 rt와 Redis에 저장된 rt가 일치하는지 검사
        // 이 로직이 없다면, 사용자가 로그아웃을 해도 공격자가 rt 유효시간이 남아있다면 무한으로 at를 발급받을 수 있는 보안 이슈 발생
        // 따라서 로그아웃시, redis 에서 해당 rt를 삭제하여 무효화 시켜야 함
        String storedRefreshToken = redisTemplate.opsForValue().get(userIdentifier);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new DomainException(DomainErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 새로운 Access Token 발급
        Authentication authentication = jwtTokenProvider.getAuthenticationFromRefreshToken(refreshToken);
        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);

        /* ===================== rt 남은 시간 < threshold 인 경우만 해당하는 로직 ====================== */

        // Sliding Refresh: RT의 남은 시간 확인
        Long expire = redisTemplate.getExpire(userIdentifier, TimeUnit.MILLISECONDS);

        // threshold 기준 이하라면 새 RT 발급 + Redis 갱신
        if (expire != null && expire < REFRESH_THRESHOLD_MS) {
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(userIdentifier);
            long newRefreshTokenExpirationMillis = jwtTokenProvider.getRefreshTokenExpirationDays() * 24 * 60 * 60 * 1000L;

            // Redis에 새 RT 저장
            redisTemplate.opsForValue().set(
                    userIdentifier,
                    newRefreshToken,
                    newRefreshTokenExpirationMillis,
                    TimeUnit.MILLISECONDS
            );

            return new AuthToken(newAccessToken, newRefreshToken);
        }

        /* ===================== rt 남은 시간 < threshold 인 경우만 해당하는 로직 ====================== */

        // 5. Access Token과 기존 Refresh Token 반환
        return new AuthToken(newAccessToken, refreshToken);
    }

    /**
     * 로그아웃 처리: Redis 에서 RefreshToken 삭제
     * @param username 로그아웃할 사용자의 로그인 이메일
     */
    public void logout(String username) {
        if (!redisTemplate.hasKey(username)) {
            throw new DomainException(DomainErrorCode.LOGINED_USER_NOT_FOUND);
        }

        redisTemplate.delete(username);
        log.info("refresh deleted: {}", username);
    }
}
