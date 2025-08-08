package com.fintory.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintory.auth.jwt.JwtTokenProvider;
import com.fintory.auth.util.OpenApiList;
import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.common.exception.ExceptionResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * 모든 HTTP 요청에 대해 JWT 토큰을 검증하고, 유효한 경우, SecurityContext에 인증 정보를 설정하는 커스텀 필터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 변환용
    private final JwtTokenProvider jwtTokenProvider;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = resolveToken(request);
            if (token == null || token.isBlank()) {
                throw new DomainException(DomainErrorCode.EMPTY_TOKEN);
            }
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("Authentication success: {}", authentication);
            }

            filterChain.doFilter(request, response);

            // 필터이기 때문에 에러가 터져도 ExceptionHandler가 잡지를 못함 -> 메서드 내에서 json 응답 객체로 변환
            // (ResponseEntity는 Spring DispatcherServlet 영역이기에 사용 불가)
        } catch (DomainException e) {
            SecurityContextHolder.clearContext();
            log.warn("JWT 인증 실패: {}", e.getErrorCode().getMessage());
            // 예외 응답 객체 생성
            ExceptionResponse exceptionResponse = new ExceptionResponse(e.getErrorCode());

            // JSON 응답 설정
            response.setStatus(e.getErrorCode().getHttpStatus().value());
            response.setContentType("application/json;charset=UTF-8");

            // 객체를 JSON 으로 변환 후 응답에 기록
            response.getWriter().write(objectMapper.writeValueAsString(exceptionResponse));
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            log.error("예상치 못한 서버 오류", e);

            ExceptionResponse exceptionResponse =
                    new ExceptionResponse(DomainErrorCode.INTERNAL_SERVER_ERROR);

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(exceptionResponse));
        }
    }

    // jwt 토큰 검증을 하지 않을 url 설정
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return Arrays.stream(OpenApiList.PUBLIC_URLS)
                .anyMatch(publicPath -> pathMatcher.match(publicPath, path));
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}