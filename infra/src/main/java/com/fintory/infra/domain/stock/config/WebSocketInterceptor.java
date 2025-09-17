package com.fintory.infra.domain.stock.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@Slf4j
public class WebSocketInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        log.info("WebSocket 핸드셰이크 헤더 정보 조회");
        log.info("요청 URI:{}", request.getURI());
        log.info("요청 메소드:{}",request.getMethod());


        request.getHeaders().forEach((headerName, headerValues) -> {
            log.info("헤더 [{}]: {}", headerName, headerValues);
        });

        log.info("Origin: {}", request.getHeaders().getOrigin());

        return true; //핸드셰이크 계속 진행

    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket 핸드셰이크 실패: ", exception);
        } else {
            log.info("WebSocket 핸드셰이크 성공!");
        }
    }
}
