package com.fintory.infra.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Primary
public class PriceQuoteWebSocketHandler extends TextWebSocketHandler {

    private Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private WebSocketSession currentSession;
    private String code;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        this.currentSession = session;

    }

    public void subscribe(String code) {
        if (!sessions.isEmpty() && currentSession != null && currentSession.isOpen()) {
            sendSubscribeMessage(code);
        }
    }

    private void sendSubscribeMessage(String code) {
        try {
            this.code = code;
            ObjectMapper mapper = new ObjectMapper();
            String approvalKey = (String) redisTemplate.opsForValue().get("kis-websocket-token");

            Map<String, Object> header = Map.of(
                    "approval_key", approvalKey,
                    "custtype", "P",
                    "tr_type", "1",
                    "content-type", "utf-8"
            );

            Map<String, Object> input = Map.of(
                    "tr_id", "H0STASP0 ",
                    "tr_key", code
            );

            Map<String, Object> body = Map.of("input", input);

            Map<String, Object> message = Map.of("header", header, "body", body);

            String jsonMessage = mapper.writeValueAsString(message);
            currentSession.sendMessage(new TextMessage(jsonMessage));
        } catch (Exception e) {
            log.error("KIS 인증 메시지 전송 실패", e);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {

        String payload = message.getPayload();

        try {
            String[] lines = payload.split("\\^");

            if (lines.length < 50) {
                log.debug("호가 데이터 라인 수 부족: {}", lines.length);
                return;
            }

            String[] headerParts = lines[0].split("\\|");
            String code = headerParts.length > 3 ? headerParts[3] : "";

            if (code.isEmpty()) return;

            Map<String, Object> priceQuote = new HashMap<>();

            log.info(code);
            // 기본 정보
            priceQuote.put("code", code);

            // 매도호가 ASKP1-ASKP10 (라인 3-12)
            for (int i = 1; i <= 10; i++) {
                priceQuote.put("ASKP" + i, getBigDecimalValue(lines, 2 + i));
            }

            // 매수호가 BIDP1-BIDP10 (라인 13-22)
            for (int i = 1; i <= 10; i++) {
                priceQuote.put("BIDP" + i, getBigDecimalValue(lines, 12 + i));
            }

            // 매도호가 잔량 ASKPRSQN1-ASKPRSQN10 (라인 23-32)
            for (int i = 1; i <= 10; i++) {
                priceQuote.put("ASKPRSQN" + i, getLongValue(lines, 22 + i));
            }

            // 매수호가 잔량 BIDPRSQN1-BIDPRSQN10 (라인 33-42)
            for (int i = 1; i <= 10; i++) {
                priceQuote.put("BIDPRSQN" + i, getLongValue(lines, 32 + i));
            }

            priceQuote.put("timestamp", System.currentTimeMillis());

            // Redis에 저장
            redisTemplate.opsForValue().set(
                    "korean-stock-quote:" + code,
                    priceQuote,
                    Duration.ofMinutes(5)
            );

            log.info("{} 호가 저장 완료 - 매도1: {}원, 매수1: {}원 매수잔량1: {} 매도잔량1:{}",
                    code, priceQuote.get("ASKP1"), priceQuote.get("BIDP1"), priceQuote.get("ASKPRSQN1"),priceQuote.get("BIDPRSQN1"));


        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // 헬퍼 메서드들
    private String getSafeValue(String[] lines, int index) {
        return (lines.length > index) ? lines[index].trim() : "0";
    }

    private BigDecimal getBigDecimalValue(String[] lines, int index) {
        try {
            String value = getSafeValue(lines, index);
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private Long getLongValue(String[] lines, int index) {
        try {
            String value = getSafeValue(lines, index);
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }

    public boolean isConnected(){
        return currentSession != null && currentSession.isOpen();
    }


}
