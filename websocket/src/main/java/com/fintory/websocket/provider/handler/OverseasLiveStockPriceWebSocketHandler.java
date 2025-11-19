package com.fintory.websocket.provider.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.websocket.LiveStockPriceStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor
public class OverseasLiveStockPriceWebSocketHandler implements WebSocketHandler {

    private Consumer<LiveStockPriceStream> dataCallBack;

    private final ObjectMapper objectMapper;
    private final RedisTemplate<Object, Object> redisTemplate;

    private WebSocketSession session;

    // 콜백 함수 설정
    public void setDataCallBack(Consumer<LiveStockPriceStream> dataCallBack) {
        this.dataCallBack = dataCallBack;
    }


    @Override
    public Mono<Void> handle(WebSocketSession session) {
        this.session = session;

        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(this:: processMessage)
                .doOnError(e->{
                    log.error("해외 주식 메시지 처리 중 에러 발생 - 에러: {}", e.getMessage());
                })
                .onErrorResume(e-> Mono.empty())
                .doOnTerminate(()->{
                    this.session= null;
                    log.info("DB증권 WebSocket 연결 종료");
                })
                .then();

    }


    public void subscribe(String code){
        if (session == null || !session.isOpen()) {
            log.warn("WebSocket이 연결되지 않음 - 종목: {}", code);
            return;
        }

         sendMessage(code,"1")
                .flatMap(message-> session.send(Mono.just(session.textMessage(message))))
                .delayElement(Duration.ofSeconds(5)) //TODO 필요없으면 지우기
                .doOnError(e->{
                    log.error("DB API 실시간 현재가 데이터 조회 메시지 요청 중 에러 발생 - 종목: {}, 에러: {}", code, e.getMessage());
                })
                .onErrorResume(e->  Mono.empty())
                 .subscribe();

    }

    public void unsubscribe(String code){
        if (session == null || !session.isOpen()) {
            log.warn("WebSocket이 연결되지 않음 - 종목: {}", code);
            return;
        }

         sendMessage(code,"2")
                .flatMap(message-> session.send(Mono.just(session.textMessage(message))))
                .delayElement(Duration.ofSeconds(5))
                .doOnError(e->{
                    log.error("DB API 실시간 현재가 데이터 구독 해제 메시지 요청 중 에러 발생 - 종목: {}, 에러: {}", code, e.getMessage());
                })
                .onErrorResume(e->  Mono.empty())
                 .subscribe();
    }

    private Mono<String> sendMessage(String code, String trType) {
        // TODO 연결 상태 확인
        /*
        *  log.warn("웹소켓이 연결되지 않아 구독 메시지를 보낼 수 없습니다. 종목: {}", code);
            throw new DomainException(DomainErrorCode.WEBSOCKET_CONNECTION_FAILED);
        * */
        return Mono.fromCallable(() -> {
                    String token = (String) redisTemplate.opsForValue().get("db-access-token");

                    if (token == null || token.trim().isEmpty()) {
                        log.error("Redis에서 DB 토큰을 찾을 수 없습니다.");
                        throw new DomainException(DomainErrorCode.TOKEN_NOT_FOUND);
                    }

                    Map<String, String> header = Map.of(
                            "token", token,
                            "tr_type", trType
                    );

                    Map<String, String> body = Map.of(
                            "tr_cd", "V60",
                            "tr_key", "FN" + code
                    );

                    Map<String, Object> request = Map.of(
                            "header", header,
                            "body", body
                    );
                    return objectMapper.writeValueAsString(request);
                })
                .subscribeOn(Schedulers.boundedElastic());

    }

    private Mono<Void> processMessage(String payload) {
        return Mono.fromCallable(() -> {
                JsonNode root = objectMapper.readTree(payload);
                JsonNode header = root.get("header");
                JsonNode body = root.get("body");

                if (header != null && header.has("tr_type")
                        && "2".equals(header.get("tr_type").asText())) {
                    log.info("구독 해제 요청 완료");
                    return null;
                }

                if(body == null || !body.has("symbol")){
                    return null;
                }
                return payload;
            })
            .flatMap(p->{
                if(p==null) return Mono.empty();
                return parseAndProcessMessage(p);
            }).subscribeOn(Schedulers.boundedElastic());

    }

    private Mono<Void> parseAndProcessMessage(String payload) {
        return Mono.fromCallable(() -> parseStockData(payload))
                .flatMap(stockData -> {
                    if (stockData == null) return Mono.empty();
                    return executeCallbacks(stockData);
                })
                .onErrorResume(e -> {
                    log.error("해외 주식 메시지 파싱 중 에러 발생: {}", e.getMessage());
                    return Mono.empty();
                });
    }

    private LiveStockPriceStream parseStockData(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode body = root.get("body");

            // 안전한 필드 추출
            String symbol = getTextValue(body, "symbol");

            BigDecimal last = parseBigDecimal(getTextValue(body, "last"));
            BigDecimal diff = parseBigDecimal(getTextValue(body, "diff"));
            BigDecimal rate = parseBigDecimal(getTextValue(body, "rate"));

            return new LiveStockPriceStream(
                    symbol.substring(2), // FN 접두사 제거
                    last,
                    diff,
                    rate
            );

        } catch (Exception e) {
            log.error("메시지 파싱 중 에러 발생: {}", e.getMessage());
            throw new DomainException(DomainErrorCode.WEBSOCKET_MESSAGE_PARSE_FAILED);
        }
    }

    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return field != null ? field.asText() : null;
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            log.warn("숫자 파싱 실패 - 값: {}, 0으로 대체", value);
            return BigDecimal.ZERO;
        }
    }

    private Mono<Void> executeCallbacks(LiveStockPriceStream stockData) {
        return Mono.fromRunnable(()->{
                if(dataCallBack!= null){
                    dataCallBack.accept(stockData);
             }
            })
                .doOnError(e -> log.error("데이터 콜백 실행 중 에러: {}", e.getMessage()))
                .onErrorResume(e->Mono.empty())
                .then();

    }

    public boolean isConnected(){
        return session != null && session.isOpen();
    }

}