package com.fintory.websocket.provider.handler;

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
public class KoreanLiveStockPriceWebSocketHandler implements WebSocketHandler {

    private final RedisTemplate<Object, Object> redisTemplate;
    private Consumer<LiveStockPriceStream> dataCallBack;
    private final ObjectMapper objectMapper;
    private WebSocketSession session;

    // 데이터를 수신받을 때마다 호출되는 콜백 함수
    public void setDataCallBack(Consumer<LiveStockPriceStream> dataCallBack) {
        this.dataCallBack = dataCallBack;
    }


    @Override
    public Mono<Void> handle(WebSocketSession session) {
        this.session = session;
        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(this::parseAndProcessMessage)
                .doOnError(e -> log.error("국내 주식 구독 메시지 처리 중 에러:{}", e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .doOnTerminate(()->{
                    this.session=null;
                    log.info("한국투자증권 WebSocket 연결 종료");
                })
                .then();
    }

    public void subscribe(String code) {
        if (session == null || !session.isOpen()) {
            log.warn("WebSocket이 연결되지 않음 - 종목: {}", code);
            return;
        }

         createSubscribeMessage(code, "1")
                .flatMap(message -> session.send(Mono.just(session.textMessage(message))))
                .delayElement(Duration.ofSeconds(5))
                .onErrorResume(e -> {
                    log.error("국내 주식 구독 실패 :{}", code);
                    return Mono.empty();
                })
                .subscribe();
    }
    public void unsubscribe(String code) {
        if (session == null || !session.isOpen()) {
            log.warn("WebSocket이 연결되지 않음 - 종목: {}", code);
            return;
        }

         createSubscribeMessage(code, "2")
                .flatMap(message -> session.send(Mono.just(session.textMessage(message))))
                .onErrorResume(e -> {
                    log.warn("구독 해제 실패: {}", code);
                    return Mono.empty();
                })
                .subscribe();
    }


    private Mono<String> createSubscribeMessage(String code, String trType) {
        return Mono.fromCallable(()->{
            String approvalKey = (String) redisTemplate.opsForValue().get("kis-websocket-access-token");

            if (approvalKey == null || approvalKey.isEmpty()) {
                log.error("Redis에서 KIS 토큰을 찾을 수 없습니다.");
                throw new DomainException(DomainErrorCode.TOKEN_NOT_FOUND);
            }

            Map<String, Object> message = Map.of(
                    "header", Map.of(
                            "approval_key", approvalKey,
                            "custtype", "P",
                            "tr_type", trType,
                            "content-type", "utf-8"
                    ),
                    "body", Map.of(
                            "input", Map.of(
                                    "tr_id", "H0STCNT0",
                                    "tr_key", code
                            )
                    )

            );
            return objectMapper.writeValueAsString(message);
        })
                .subscribeOn(Schedulers.boundedElastic());

    }

    private Mono<Void> parseAndProcessMessage(String payload) {
        return Mono.fromCallable(() -> {
                   return parseStockData(payload);
                })
                .flatMap(stockData -> {
                    if (stockData == null) return Mono.empty();
                    // 콜백 실행
                    return executeCallbacks(stockData);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    log.error("구독 메시지 파싱 실패: {} ", e.getMessage());
                    return Mono.empty();
                });
    }

    private LiveStockPriceStream parseStockData(String body) {
        try {
            String[] fields = body.split("\\^");
            if (fields.length < 40) return null;

            String codeField = fields[0].trim();
            String code = codeField.contains("|")
                    ? codeField.split("\\|")[3]
                    : codeField;

            BigDecimal price = parseBigDecimal(fields[2]);
            BigDecimal change = parseBigDecimal(fields[4].replace("+", ""));
            BigDecimal changePercent = parseBigDecimal(fields[5].replace("%", "").replace("+", ""));

            LiveStockPriceStream stockData = new LiveStockPriceStream(
                    code, price, change, changePercent
            );

            return stockData;

        } catch (Exception e) {
            log.error("KIS Developer 실시간 현재가 조회 시 응답 받는 과정에서 에러 발생:{}", e.getMessage());
            throw new DomainException(DomainErrorCode.WEBSOCKET_MESSAGE_PARSE_FAILED);
        }
    }


    private BigDecimal parseBigDecimal(String value){
        try{
            return new BigDecimal(value);
        }catch(NumberFormatException e){
            log.warn("숫자 파싱 실패 - 값: {}, 0으로 대체", value);
            return BigDecimal.ZERO;
        }
    }


    private Mono<Void> executeCallbacks(LiveStockPriceStream stockData) {
        return Mono.fromRunnable(() -> {
                    if (dataCallBack != null) {
                        dataCallBack.accept(stockData);
                    }
                })
                .doOnError(e -> log.error("국내 주식 데이터 콜백 실행 중 에러 :{}", e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .then();
    }
    public boolean isConnected(){
        return session != null && session.isOpen();
    }
}