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

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Primary
public class OverseasRealPriceWebSocketHandler extends TextWebSocketHandler {
    private Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private WebSocketSession currentSession;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    //클라이언트가 연결되었을 때 실행
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        this.currentSession = session;
    }

    public void subscribeStock(String code) throws IOException {
        if(!sessions.isEmpty() && currentSession!=null && currentSession.isOpen()){
            sendSubscribeMessage(code);
        }
    }
    private void sendSubscribeMessage(String code) throws IOException {

        try{

            ObjectMapper objectMapper = new ObjectMapper();
            String approvalKey = (String) redisTemplate.opsForValue().get("kis-websocket-token");

            Map<String, Object> header = Map.of(
                    "approval_key", approvalKey,
                    "custtype","P",
                    "tr_type","1",
                    "content-type","utf-8"
            );

            Map<String, Object> input = Map.of(
                    "tr_id","HDFSCNT0",
                    "tr_key","DNAS"+code
            );

            Map<String, Object> body = Map.of("input",input);

            Map<String, Object> message = Map.of(
                    "header",header,
                    "body",body
            );

            String jsonMessage = objectMapper.writeValueAsString(message);
            currentSession.sendMessage(new TextMessage(jsonMessage));

        }catch(Exception e){
            log.error("KIS 인증 메시지 전송 실패", e);
        }

    }

    //클라이언트로부터 메시지를 받았을 때 실행
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();

        try {
            if (!payload.trim().startsWith("{") || !payload.trim().endsWith("}")) {
                //log.info("JSON이 아닌 메시지 수신: {}", payload);
            }

            String[] fields = payload.split("\\^");

            for(int i=0;i<fields.length;i++){
                log.info(fields[i]);
            }

            if(fields.length>=25) {
                String stockCode = fields[1];
                String currentPrice = fields[11];
                String priceChange = fields[13];
                String priceChangeRate = fields[14];


                Map<String,Object> realPriceMap = new HashMap<>();
                realPriceMap.put("code",stockCode);
                realPriceMap.put("currentPrice",currentPrice);
                realPriceMap.put("priceChange",priceChange);
                realPriceMap.put("priceChangeRate",priceChangeRate);


                redisTemplate.opsForValue().set(
                        "overseas-stock-realprice:"+stockCode,
                        realPriceMap,
                        Duration.ofMinutes(5)
                );
                log.info("{} - 현재가:{} 전일대비:{} 등락률:{}",
                        stockCode, realPriceMap.get("currentPrice"), realPriceMap.get("priceChange"),  realPriceMap.get("priceChangeRate"));
            }

        } catch (Exception e) {
            System.out.println(" 메시지 처리 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //연결이 끊어졌을 때 실행
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }

    public boolean isConnected(){
        return currentSession != null && currentSession.isOpen();
    }

}