package com.fintory.auth.service.authservice;

import com.fintory.auth.dto.AuthToken;
import com.fintory.auth.dto.request.SignUpRequest;
import com.fintory.auth.jwt.JwtTokenProvider;
import com.fintory.auth.util.CustomUserDetails;
import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.child.model.Status;
import com.fintory.domain.common.Role;
import com.fintory.domain.parent.model.Parent;
import com.fintory.infra.domain.parent.repository.ParentRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ParentAuthServiceImpl implements AuthService {

    private static final long REFRESH_THRESHOLD_MS = 2L * 24 * 60 * 60 * 1000;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final ParentRepository parentRepository;

    @Override
    @Transactional
    public AuthToken signup(SignUpRequest request) {


        if (checkDuplicateEmail(request.email())) {
            throw new DomainException(DomainErrorCode.ALREADY_REGISTERED_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        Parent parent = Parent.createWithIdPw(
                request.nickname(),
                request.email(),
                encodedPassword,
                Role.PARENT,
                Status.ACTIVE
                );

        parentRepository.save(parent);

        return login(request.email(), request.password());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkDuplicateEmail(String email) {
        return parentRepository.findByEmail(email).isPresent();
    }

    @Override
    public AuthToken login(String email, String password) {

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            Object principal = authentication.getPrincipal();

            if (principal instanceof CustomUserDetails customUserDetails) {
                log.info("인증 직후 password 값: {}", customUserDetails.getPassword());
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("authentication: {}", authentication);

            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication.getName());
            long refreshTokenExpirationMillis =
                    jwtTokenProvider.getRefreshTokenExpirationDays() * 24 * 60 * 60 * 1000L;

            redisTemplate.opsForValue().set(
                    authentication.getName(),
                    refreshToken,
                    refreshTokenExpirationMillis,
                    TimeUnit.MILLISECONDS
            );

            return new AuthToken(accessToken, refreshToken);
    }

    @Override
    public AuthToken reissue(String refreshToken) {

        jwtTokenProvider.validateToken(refreshToken);

        Claims claims = jwtTokenProvider.parseClaims(refreshToken);
        String userIdentifier = claims.getSubject();
        log.info("userIdentifier: {}", userIdentifier);

        if (!"refresh".equals(jwtTokenProvider.getTokenCategory(refreshToken))) {
            throw new DomainException(DomainErrorCode.INVALID_TOKEN_TYPE);
        }

        String storedRefreshToken = redisTemplate.opsForValue().get(userIdentifier);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new DomainException(DomainErrorCode.INVALID_REFRESH_TOKEN);
        }

        Authentication authentication = jwtTokenProvider.getAuthenticationFromRefreshToken(refreshToken);
        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);

        Long expire = redisTemplate.getExpire(userIdentifier, TimeUnit.MILLISECONDS);

        if (expire != null && expire < REFRESH_THRESHOLD_MS) {
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(userIdentifier);
            long newRefreshTokenExpirationMillis = jwtTokenProvider.getRefreshTokenExpirationDays() * 24 * 60 * 60 * 1000L;

            redisTemplate.opsForValue().set(
                    userIdentifier,
                    newRefreshToken,
                    newRefreshTokenExpirationMillis,
                    TimeUnit.MILLISECONDS
            );

            return new AuthToken(newAccessToken, newRefreshToken);
        }

        return new AuthToken(newAccessToken, refreshToken);
    }

    @Override
    public void logout(String username) {
        if (!redisTemplate.hasKey(username)) {
            throw new DomainException(DomainErrorCode.LOGINED_USER_NOT_FOUND);
        }

        redisTemplate.delete(username);
        log.info("refresh deleted: {}", username);
    }
}
