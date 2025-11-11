package com.fintory.websocket.provider.service;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.websocket.LiveStockPriceStream;
import com.fintory.websocket.provider.handler.KoreanLiveStockPriceWebSocketHandler;
import com.fintory.websocket.provider.handler.OverseasLiveStockPriceWebSocketHandler;
import com.fintory.websocket.publisher.service.StockDataProcessService;
import com.fintory.websocket.publisher.state.StockDataHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.client.WebSocketConnectionManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Service
@Slf4j
public class WebSocketConnectionService {

    private final RedisTemplate<Object, Object> redisTemplate;
    @Value("${db-openapi.base-url}")
    private String baseUrl;

    private final StockDataHolder stockDataHolder;
    private final WebSocketConnectionManager koreanConnectionManager;
    private final WebSocketConnectionManager overseasConnectionManager;
    private final StockDataProcessService stockDataProcessService;
    private final RestTemplate restTemplate;
    private final KoreanLiveStockPriceWebSocketHandler koreanHandler;
    private final OverseasLiveStockPriceWebSocketHandler overseasHandler;

    public WebSocketConnectionService(StockDataHolder stockDataHolder,
                                      @Qualifier("koreanLiveStockPriceWebSocketConnectionManager")WebSocketConnectionManager koreanConnectionManager,
                                      @Qualifier("overseasLiveStockPriceWebSocketConnectionManager")WebSocketConnectionManager overseasConnectionManager,
                                      StockDataProcessService stockDataProcessService, RestTemplate restTemplate,
                                      KoreanLiveStockPriceWebSocketHandler koreanHandler,
                                      OverseasLiveStockPriceWebSocketHandler overseasHandler,
                                      RedisTemplate<Object, Object> redisTemplate) {
        this.stockDataHolder = stockDataHolder;
        this.koreanConnectionManager = koreanConnectionManager;
        this.overseasConnectionManager = overseasConnectionManager;
        this.stockDataProcessService = stockDataProcessService;
        this.restTemplate = restTemplate;
        this.koreanHandler = koreanHandler;
        this.overseasHandler = overseasHandler;
        this.redisTemplate = redisTemplate;
    }


    /* WebSocket 연결 관리 */
    public void connectKoreanWebSocket() {
        if (stockDataHolder.getIsKoreanConnected().get()) {
            return;
        }

        Consumer<LiveStockPriceStream> callback = dto ->
                stockDataProcessService.processStreamData(dto,
                        stockDataHolder.getPreviousKoreanData(),
                        stockDataHolder.getKoreanPendingData(), "국내");

        koreanHandler.setDataCallBack(callback);
        koreanConnectionManager.start();

        boolean connected = koreanHandler.waitForConnection(30);
        if (!connected) {
            log.error("연결 실패!");
            throw new DomainException(DomainErrorCode.WEBSOCKET_CONNECTION_FAILED);
        }

        stockDataHolder.getIsKoreanConnected().set(true);
        log.info("국내 주식 WebSocket 연결 완료");
    }


    public void connectOverseasWebSocket() {
        if (stockDataHolder.getIsOverseasConnected().get()) {
            return;
        }
        Consumer<LiveStockPriceStream> callback = dto ->
                stockDataProcessService.processStreamData(dto, stockDataHolder.getPreviousOverseasData(), stockDataHolder.getOverseasPendingData(), "해외");

        overseasHandler.setDataCallBack(callback);
        overseasConnectionManager.start();

        boolean connected = overseasHandler.waitForConnection(30);
        if (!connected) {
            log.info("해외 장시간임에도 WebSocket 연결 실패 - 공휴일이거나 기술적 문제일 수 있음");
            throw new DomainException(DomainErrorCode.WEBSOCKET_CONNECTION_FAILED);
        }

        stockDataHolder.getIsOverseasConnected().set(true);
        log.info("해외 주식 WebSocket 연결 완료");
    }




    public void disconnectKoreanWebSocket() {
        if (!stockDataHolder.getIsKoreanConnected().get()) return;

        log.info("국내 WebSocket 연결 해제 시작");
        try {
            new ArrayList<>(stockDataHolder.getKoreanSubscribedStocks()).forEach(code -> {
                try {
                    koreanHandler.unsubscribe(code);
                    stockDataHolder.getKoreanSubscribedStocks().remove(code);
                } catch (Exception e) {
                    log.warn("국내 종목 {} 구독 해제 중 에러 발생: {}", code, e.getMessage());
                }
            });
            Thread.sleep(1000); //서버 처리 대기
        } catch (Exception e) {
            log.error("구독 해제 중 에러: {}", e.getMessage());
        } finally {
            // 반드시 실행
            koreanConnectionManager.stop();
            stockDataHolder.getKoreanSubscribedStocks().clear();
            stockDataHolder.getPreviousKoreanData().clear();
            stockDataHolder.getKoreanPendingData().clear();
        }

        log.info("국내 WebSocket 연결 해제 완료");
    }


    public void disconnectOverseasWebSocket() {
        if (!stockDataHolder.getIsOverseasConnected().get()) return;

        log.info("해외 WebSocket 연결 해제 시작");
        try {
            new ArrayList<>(stockDataHolder.getOverseasSubscribedStocks()).forEach(code -> {
                try {
                    //overseasHandler.unsubscribe(code);
                    stockDataHolder.getOverseasSubscribedStocks().remove(code);
                } catch (Exception e) {
                    log.warn("해외 종목 {} 구독 해제 중 에러 발생: {}", code, e.getMessage());
                }
            });
            Thread.sleep(1000); //서버 처리 대기
            try {
                disconnectDBSession();  //db증권은 세션 정리를 하지 않을 경우 에러 발생함
                Thread.sleep(500);
            } catch (Exception e) {
                log.warn("세션 종료 실패 (무시): {}", e.getMessage()); // 에러 무시
            }
        } catch (Exception e) {
            log.error("구독 해제 중 에러: {}", e.getMessage());
        } finally {
            overseasConnectionManager.stop();
            stockDataHolder.getIsOverseasConnected().set(false);
            stockDataHolder.getOverseasSubscribedStocks().clear();
            stockDataHolder.getPreviousOverseasData().clear();
            stockDataHolder.getOverseasPendingData().clear();

        }

        log.info("해외 WebSocket 연결 해제 완료");
    }


    public void disconnectDBSession(){
        try {
            //ERROR LettuceConnectionFactory has been STOPPED. Use start() to initialize it
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String token = stockDataHolder.getCachedAccessToken();
            headers.set("authorization", "Bearer " +token);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(new HashMap<>(), headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/api/v1/websocket/disconnectSession",
                    entity,
                    Map.class
            );
            log.info(response.getBody().toString());
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();
                log.info("웹소켓 세션 초기화 성공: {}", body.get("result"));
            } else {
                log.warn("웹소켓 세션 초기화 응답 이상: {}", response.getStatusCode());
            }

        }catch (Exception e) {
            log.error("웹소켓 세션 초기화 중 오류 발생", e);
            throw new RuntimeException("웹소켓 세션 초기화 실패: " + e.getMessage());
        }
    }


}
