package com.fintory.websocket.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@DependsOn("liveStockPriceWebSocketService")
public class SecurityConfig {


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    /**
     * CORS 설정을 위한 CorsConfigurationSource 빈을 정의
     * JWT 인증을 위해 특정 헤더를 허용하도록 설정
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 프론트 도메인 설정
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:8081",
                "https://localhost:8081",
                "http://fintory.xyz",
                "https://fintory.xyz"
        ));
        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // 자격 증명(쿠키, HTTP 인증 등) 허용
        configuration.setAllowCredentials(true);
        // 모든 헤더 허용
        configuration.setAllowedHeaders(List.of("*"));
        // 클라이언트가 접근할 수 있도록 노출할 응답 헤더
        configuration.setExposedHeaders(Arrays.asList("Authorization", "AccessToken", "RefreshToken"));

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }


}
