package com.fintory.auth.jwt;

import com.fintory.auth.service.CustomUserDetailsService;
import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenExpirationMin;
    private final long refreshTokenExpirationDays;
    private final CustomUserDetailsService customUserDetailsService;

    @Autowired
    public JwtTokenProvider(
            @Value("${jwt.secret}") String key,
            @Value("${jwt.access-token-expiration-minutes}") long accessTokenExpirationMin,
            @Value("${jwt.refresh-token-expiration-days}") long refreshTokenExpirationDays,
            CustomUserDetailsService customUserDetailsService
    ){
        byte[] keyBytes = Decoders.BASE64.decode(key);
        this.key = new SecretKeySpec(keyBytes, Jwts.SIG.HS256.key().build().getAlgorithm());
        this.accessTokenExpirationMin = accessTokenExpirationMin;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * Access Token 생성
     * @param authentication 인증 객체 (인증에 대한 모든 정보)
     * @return 생성된 Access Token 문자열
     */
    public String generateAccessToken(Authentication authentication) {

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String subject = authentication.getName();
        String authorityString = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMin * 60 * 1000);

        return Jwts.builder()
                .subject(subject) // 사용자 ID (loginId)
                .claim("auth", authorityString) // 권한 정보
                .claim("category", "access") // 토큰 카테고리 (access)
                .issuedAt(now) // 발행 시간
                .expiration(expiryDate) // 만료 시간
                .signWith(key) // 서명
                .compact();
    }

    /**
     * Refresh Token 생성
     * @param email 사용자 고유 식별자 이메일
     * @return 생성된 Refresh Token 문자열
     */
    public String generateRefreshToken(String email) {
        // Refresh Token은 권한 정보를 포함하지 않고, 단순히 사용자 식별 목적으로만 사용
        return Jwts.builder()
                .subject(email) // 고유 식별자만 포함
                .claim("category", "refresh")
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationDays * 24 * 60 * 60 * 1000))
                .signWith(key)
                .compact();
    }

    /**
     * AT 에서 인증 정보 조회
     * 토큰을 파싱하여 시큐리티가 인식가능한 인증객체로 반환
     * @param token JWT 토큰 문자열 (Access Token)
     * @return Spring Security의 Authentication 객체
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        if (claims.get("auth") == null) {
            throw new JwtException("권한 정보가 없는 토큰입니다.");
        }

        String email = claims.getSubject();
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    /**
     * RT 에서 인증 정보 조회
     * 토큰을 파싱하여 시큐리티가 인식가능한 인증객체로 반환
     * @param token JWT 토큰 문자열 (Refresh Token)
     * @return Spring Security의 Authentication 객체
     */
    public Authentication getAuthenticationFromRefreshToken(String token) {
        Claims claims = parseClaims(token);

        // 권한 정보 없음: rt는 권한이 없고 사용자 식별만 하면 되니까 subject 만으로 인증 객체 생성
        String email = claims.getSubject();
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    /**
     * JWT 토큰 유효성 검증
     * @param token 검증할 JWT 토큰 문자열
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key) // key는 SecretKey or PublicKey
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            throw new DomainException(DomainErrorCode.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new DomainException(DomainErrorCode.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new DomainException(DomainErrorCode.UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.EMPTY_TOKEN);
        }
    }


    /**
     * Access Token에서 Claims 정보 추출 (만료된 토큰에서도 클레임 가져올 수 있도록)
     * @param token 토큰 문자열
     * @return 토큰의 클레임 (Payload)
     */
    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰의 경우에도 클레임 정보는 가져올 수 있도록 처리
            return e.getClaims();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT 토큰 파싱 실패: {}", e.getMessage());
            // 토큰이 유효하지 않으면 INVALID_TOKEN 예외를 던져 상위에서 처리
            throw new DomainException(DomainErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * 토큰에서 사용자 식별자(subject) 추출
     */
    public String getUserIdentifier(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 토큰 카테고리 (access/refresh) 추출
     */
    public String getTokenCategory(String token) {
        return parseClaims(token).get("category", String.class);
    }
}
