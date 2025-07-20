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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Primary
public class RealPriceWebSocketHandler extends TextWebSocketHandler {

    private Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private WebSocketSession currentSession;
    private Set<String> subscribedStocks = ConcurrentHashMap.newKeySet(); //구독 목록

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
            subscribedStocks.add(code);
        }
    }
    private void sendSubscribeMessage(String code) throws IOException {

        try{

            ObjectMapper objectMapper = new ObjectMapper();
            String approvalKey = (String) redisTemplate.opsForValue().get("kis-websocket-token");
            System.out.println("---------------------------------------");
            System.out.println(approvalKey);
            Map<String, Object> header = Map.of(
                    "approval_key", approvalKey,
                    "custtype","P",
                    "tr_type","1",
                    "content-type","utf-8"
            );

            Map<String, Object> input = Map.of(
                    "tr_id","H0STCNT0",
                    "tr_key",code
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
            String[] fields = payload.split("\\^");

             if(fields.length<40){
                 log.debug("국내 주식 현재가 라인 수 부족",fields.length);
                 return;
             }
            log.info("fields[5] 원본 값: '{}'", fields[5]);
            String cleaned = fields[5].replace("%","").replace("+","");
            log.info("정제된 값: '{}'", cleaned);

             Map<String, Object> koreanStockRealPrice = new HashMap<>();
            koreanStockRealPrice.put("currentPrice", fields[2]);
            koreanStockRealPrice.put("priceChangeRate", fields[5].replace("%",""));
            koreanStockRealPrice.put("priceChangeSign", fields[3]);
            koreanStockRealPrice.put("priceChange", fields[4]);

            // 종목코드 추출 (파이프로 분리된 경우)
            String codeField = fields[0].trim();
            String code;
            if (codeField.contains("|")) {
                String[] codeParts = codeField.split("\\|"); // \\ 추가!
                code = codeParts.length > 3 ? codeParts[3] : codeField;
            } else {
                code = codeField;
            }
            koreanStockRealPrice.put("code", code);
            koreanStockRealPrice.put("time", fields[1]);

            // Redis에 저장
            redisTemplate.opsForValue().set(
                    "korean-stock-realprice:" + code,
                    koreanStockRealPrice,
                    Duration.ofMinutes(5)
            );

            // 005930 실시간: 66700.0원 (2000.0원, 3.09% )
            log.info(" {} 실시간: {}원 ({}원, {}% )",
                    code,
                    koreanStockRealPrice.get("currentPrice"),
                    koreanStockRealPrice.get("priceChange"),
                    koreanStockRealPrice.get("priceChangeRate")
                    );

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

   public void unsubscribedStock(String code){
        try{
            if(!sessions.isEmpty() && currentSession!=null && currentSession.isOpen()){
                sendUnsubscribeMessage(code);
                subscribedStocks.remove(code);

                redisTemplate.delete("korean-stock-realprice:" + code);
            }
        }catch (Exception e){
            log.error("구독 해제 실패: "+code,e);
        }
   }


   public void sendUnsubscribeMessage(String code){
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            String approvalKey = (String) redisTemplate.opsForValue().get("kis-websocket-token");

            Map<String, Object> header = Map.of(
                    "approval_key", approvalKey,
                    "custtype","P",
                    "tr_type","2", // 구독 해제는 "2"
                    "content-type","utf-8"
            );

            Map<String, Object> input = Map.of(
                    "tr_id","H0STCNT0",
                    "tr_key",code
            );

            Map<String, Object> body = Map.of("input",input);

            Map<String, Object> message = Map.of(
                    "header",header,
                    "body",body
            );

            String jsonMessage = objectMapper.writeValueAsString(message);
            currentSession.sendMessage(new TextMessage(jsonMessage));

        }catch(Exception e){
            log.error("구독 해제 메시지 전송 실패", e);
        }
   }

   public void unsubscribeAll(){
        Set<String> stocksToUnsubscribe = new HashSet<>(subscribedStocks);
        stocksToUnsubscribe.forEach(this::unsubscribedStock);
   }
}
