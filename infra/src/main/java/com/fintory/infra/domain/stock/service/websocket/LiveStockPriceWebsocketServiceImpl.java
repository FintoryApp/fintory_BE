package com.fintory.infra.domain.stock.service.websocket;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.websocket.LiveStockPriceStream;
import com.fintory.domain.stock.dto.websocket.MarketStatusResponse;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.service.websocket.LiveStockPriceWebSocketSaverService;
import com.fintory.domain.stock.service.websocket.LiveStockPriceWebsocketService;
import com.fintory.infra.domain.alarm.event.PriceAlertEvent;
import com.fintory.infra.domain.stock.handler.KoreanLiveStockPriceWebSocketHandler;
import com.fintory.infra.domain.stock.handler.OverseasLiveStockPriceWebSocketHandler;
import com.fintory.infra.domain.stock.repository.StockRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.client.WebSocketConnectionManager;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

//NOTE 구독 시도시 -> 에러 코드를 보고 프론트에서 DB API 호출
//NOTE 구독 성공 후 일정시간 동안 데이터가 오지 않으면 -> 프론트에서 연결 끊김 판단
@Service
@Slf4j
public class LiveStockPriceWebsocketServiceImpl implements LiveStockPriceWebsocketService {

    @Value("${db-openapi.base-url}")
    private String baseUrl;

    private final RedisTemplate<Object, Object> redisTemplate;
    private final WebSocketConnectionManager koreanConnectionManager;
    private final WebSocketConnectionManager overseasConnectionManager;
    private final KoreanLiveStockPriceWebSocketHandler koreanHandler;
    private final OverseasLiveStockPriceWebSocketHandler overseasHandler;
    private final StockRepository stockRepository;
    private final SimpMessagingTemplate messageTemplate;

    // 공통 데이터 구조들
    //구독 중인 종목 코드 저장
    private final Set<String> koreanSubscribedStocks = ConcurrentHashMap.newKeySet();
    private final Set<String> overseasSubscribedStocks = ConcurrentHashMap.newKeySet();

    //db에 저장되지 않은 주식 데이터 임시 저장용
    private final Map<String, LiveStockPriceStream> koreanPendingData = new ConcurrentHashMap<>();
    private final Map<String, LiveStockPriceStream> overseasPendingData = new ConcurrentHashMap<>();

    // 이전에 받은 주식 데이터 저장 -> 중복 데이터 필터링용
    private final Map<String, LiveStockPriceStream> previousKoreanData = new ConcurrentHashMap<>();
    private final Map<String, LiveStockPriceStream> previousOverseasData = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate;

    private volatile AtomicBoolean isKoreanConnected = new AtomicBoolean(false);
    private volatile AtomicBoolean  isOverseasConnected = new AtomicBoolean(false);
    private String cachedAccessToken;

    private final LiveStockPriceWebSocketSaverService liveStockPriceWebSocketSaverService;

    //이벤트
    private final ApplicationEventPublisher applicationEventPublisher;

    public LiveStockPriceWebsocketServiceImpl(
            @Qualifier("koreanLiveStockPriceWebSocketConnectionManager") WebSocketConnectionManager koreanConnectionManager,
            @Qualifier("overseasLiveStockPriceWebSocketConnectionManager") WebSocketConnectionManager overseasConnectionManager,
            KoreanLiveStockPriceWebSocketHandler koreanHandler,
            OverseasLiveStockPriceWebSocketHandler overseasHandler,
            StockRepository stockRepository,
            SimpMessagingTemplate messageTemplate, RestTemplate restTemplate, RedisTemplate<Object, Object> redisTemplate, LiveStockPriceWebSocketSaverService liveStockPriceWebSocketSaverService, ApplicationEventPublisher applicationEventPublisher) {

        this.koreanConnectionManager = koreanConnectionManager;
        this.overseasConnectionManager = overseasConnectionManager;
        this.koreanHandler = koreanHandler;
        this.overseasHandler = overseasHandler;
        this.stockRepository = stockRepository;
        this.messageTemplate = messageTemplate;
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
        this.liveStockPriceWebSocketSaverService = liveStockPriceWebSocketSaverService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /* 구독 관리 메서드 */
    @Override
    public void koreanStockSubscribe(String code) {
        subscribeStock(code, "국내", isKoreanConnected, koreanSubscribedStocks,
                this::connectKoreanWebSocket, koreanHandler::subscribe);
    }

    @Override
    public void koreanStockUnsubscribe(String code) {
        unsubscribeStock(code, "국내", koreanSubscribedStocks, previousKoreanData,
                koreanPendingData, koreanHandler::unsubscribe);
    }

    @Override
    public void overseasStockSubscribe(String code) {
        subscribeStock(code, "해외", isOverseasConnected, overseasSubscribedStocks,
                this::connectOverseasWebSocket, overseasHandler::subscribe);
    }

    @Override
    public void overseasStockUnsubscribe(String code) {
        unsubscribeStock(code, "해외", overseasSubscribedStocks, previousOverseasData,
                overseasPendingData, overseasHandler::unsubscribe);
    }

    @Override
    public void sendStockData(String stockCode, Object stockData) {
        if (stockData instanceof LiveStockPriceStream stream) {
            if (stream.priceChange() == null || stream.priceChange().compareTo(BigDecimal.ZERO) == 0) {
                log.debug("변동 없음 - 전송 스킵: {}", stockCode);
                return;
            }
            messageTemplate.convertAndSend("/topic/stock/live-Price/" + stockCode, stockData);
        }
    }

    /* 구독 자동 실행 메소드 */
    @Scheduled(cron="0 30 09 * * MON-FRI", zone="America/New_York")
    public void scheduledOverseasMarketSubscription(){
            startOverseasMarketSubscription();
    }

    @Scheduled(cron="0 0 9 * * MON-FRI", zone="Asia/Seoul")
    public void scheduledKoreanMarketSubscription(){
            startKoreanMarketSubscription();
    }

    @PostConstruct
    public void initMarketSubscriptions() {
        // 국내 장 체크 및 구독
        if (isKoreanMarketOpen()) {
            log.info("애플리케이션 시작 - 국내 장 열림, 자동 구독 시작");
            startKoreanMarketSubscription();
        } else {
            log.info("국내 장이 열려있지 않아 자동 구독 스킵");
        }

        // 해외 장 체크 및 구독
        if (isOverseasMarketOpen()) {
            log.info("애플리케이션 시작 - 해외 장 열림, 자동 구독 시작");
            startOverseasMarketSubscription();
        } else {
            log.info("해외 장이 열려있지 않아 자동 구독 스킵");
        }
    }

    /* 통합 구독/구독해제 로직 */
    private void subscribeStock(String code, String marketName, AtomicBoolean isConnected,
                                Set<String> subscribedStocks, Runnable connectAction,
                                Consumer<String> subscribeAction) {
        try {

            //해외, 국내 주식 토큰 분리
            if("해외".equals(marketName)){
                cachedAccessToken = (String) redisTemplate.opsForValue().get("db-access-token");
            }else{
                cachedAccessToken = (String) redisTemplate.opsForValue().get("kis-websocket-access-token");
            }

            boolean isMarketClosed = ("해외".equals(marketName) && !isOverseasMarketOpen()) ||
                    ("국내".equals(marketName) && !isKoreanMarketOpen());

            if (isMarketClosed) {
                throw new DomainException(DomainErrorCode.MARKET_CLOSED);
            }

            if (!isConnected.get()) {
                log.info("{} 주식 WebSocket이 연결되어 있지 않아 자동 연결을 시작합니다.", marketName);
                connectAction.run();
            }

            if (!subscribedStocks.contains(code)) {
                subscribedStocks.add(code);
            }
            log.info("{} 종목 {} 구독", marketName, code);

            subscribeAction.accept(code);
        } catch (Exception e) {
            subscribedStocks.remove(code);
            log.error("{} 종목 구독 실패: {}", marketName, e.getMessage());
            throw new DomainException(DomainErrorCode.STOCK_SUBSCRIBE_FAILED);
        }
    }

    private void unsubscribeStock(String code, String marketName, Set<String> subscribedStocks,
                                  Map<String, LiveStockPriceStream> previousData,
                                  Map<String, LiveStockPriceStream> pendingData,
                                  Consumer<String> unsubscribeAction) {
        try {
            if (subscribedStocks.contains(code)) {
                unsubscribeAction.accept(code);
                subscribedStocks.remove(code);

                // 메모리 정리
                previousData.remove(code);
                pendingData.remove(code);
                log.info("{} 종목 {} 구독 해제", marketName, code);
            }
        } catch (Exception e) {
            log.error("{} 종목 구독 해제 실패: {}", marketName, e.getMessage());
            throw new DomainException(DomainErrorCode.STOCK_UNSUBSCRIBE_FAILED);
        }
    }

    /* WebSocket 연결 관리 */
    private void connectKoreanWebSocket() {
        if (isKoreanConnected.get()) {
            log.info("국내 주식 WebSocket이 이미 연결되어 있습니다.");
            return;
        }

        Consumer<LiveStockPriceStream> callback = dto ->
                processStreamData(dto, previousKoreanData, koreanPendingData, "국내");

        koreanHandler.setDataCallBack(callback);
        koreanConnectionManager.start();

        boolean connected = koreanHandler.waitForConnection(30);
        if (!connected) {
            log.info("국내 장시간임에도 WebSocket 연결 실패 - 공휴일이거나 기술적 문제일 수 있음");
            throw new DomainException(DomainErrorCode.WEBSOCKET_CONNECTION_FAILED);
        }

        isKoreanConnected.set(true);
        log.info("국내 주식 WebSocket 연결 완료");
    }

    private void connectOverseasWebSocket() {
        if (isOverseasConnected.get()) {
            log.info("해외 주식 WebSocket이 이미 연결되어 있습니다.");
            return;
        }

        Consumer<LiveStockPriceStream> callback = dto ->
                processStreamData(dto, previousOverseasData, overseasPendingData, "해외");

        overseasHandler.setDataCallBack(callback);
        overseasConnectionManager.start();

        boolean connected = overseasHandler.waitForConnection(30);
        if (!connected) {
            log.info("해외 장시간임에도 WebSocket 연결 실패 - 공휴일이거나 기술적 문제일 수 있음");
            throw new DomainException(DomainErrorCode.WEBSOCKET_CONNECTION_FAILED);
        }

        isOverseasConnected.set(true);
        log.info("해외 주식 WebSocket 연결 완료");
    }

    //웹소켓으로 받은 데이터를 처리하는 메서드
    private void processStreamData(LiveStockPriceStream dto,
                                   Map<String, LiveStockPriceStream> previousData,
                                   Map<String, LiveStockPriceStream> pendingData,
                                   String marketName) {
        LiveStockPriceStream previous = previousData.get(dto.code());

        //이전 데이터와 비교하여 중복 체크
        if (previous != null && previous.equals(dto)) {
            log.debug("{} 주식 중복 데이터 스킵: {}", marketName, dto.code());
            return; //똑같은 데이터면 무시
        }

        //새로운 데이터를 받으면 -> 감시가 이벤트 발행
        applicationEventPublisher.publishEvent(
                new PriceAlertEvent(this,dto)
        );

        //스케쥴러 + 웹소켓 연결 시작하자마자 받은 데이터 값(첫 데이터) 저장
        if(previous == null) {
            try {
                liveStockPriceWebSocketSaverService.saveStockData(dto); //DB에 바로 저장
                log.debug("{} 종목 {} 실시간 저장 완료", marketName, dto.code());
            } catch (Exception e) {
                // 실패 시 배치 저장을 위해 pendingData에 보관
                pendingData.put(dto.code(), dto);
                log.error("{} 종목 {} 실시간 저장 실패, 배치 저장 대기: {}", marketName, dto.code(), e.getMessage());
            }
        }

        //새로운 데이터면 다음 중복 체크용으로 저장
        previousData.put(dto.code(), dto);
        pendingData.put(dto.code(), dto); //배치 저장 대기
        sendStockData(dto.code(), dto); //클라이언트에게 전송
    }

    /* 스케줄링 - 배치 저장 */
    @Scheduled(cron = "0 * 9-15 * * MON-FRI", zone = "Asia/Seoul")
    public void saveKoreanStockDataBatch() {
        if (!isKoreanMarketOpen()) {
            log.debug("국내 장 마감으로 인한 배치 저장 중단");
            return;
        }
        saveBatchData("국내", koreanPendingData);
    }

    @Scheduled(cron = "0 * 9-15 * * MON-FRI", zone = "America/New_York")
    public void saveOverseasStockDataBatch() {
        if (!isOverseasMarketOpen()) {
            log.debug("해외 장 마감으로 인한 배치 저장 중단");
            return;
        }
        saveBatchData("해외", overseasPendingData);
    }

    private void saveBatchData(String marketName, Map<String, LiveStockPriceStream> pendingData) {
        if (pendingData.isEmpty()) return;

        Map<String, LiveStockPriceStream> dataToSave = new HashMap<>(pendingData);
        pendingData.clear();

        dataToSave.values().forEach(dto -> {
            try {
                liveStockPriceWebSocketSaverService.saveStockData(dto);
            } catch (Exception e) {
                log.error("{} 종목 {} 저장 실패: {}", marketName, dto.code(), e.getMessage());
            }
        });

        log.info("{} 주식 배치 저장 완료 - 저장된 종목 수: {}", marketName, dataToSave.size());
    }

    /*  장 시작 시 자동으로 필요한 종목 전부 구독*/
    public void startKoreanMarketSubscription(){
        if (!isKoreanMarketOpen()) {
            log.info("국내 장이 열려있지 않아 자동 구독 스킵");
            return;
        }

        connectKoreanWebSocket();

        List<Stock> targetStocks = stockRepository.findByCurrencyName("KRW");
        int beforeSize = koreanSubscribedStocks.size();

        targetStocks.forEach(dto -> {
            if(!koreanSubscribedStocks.contains(dto.getCode())) {
                try {
                    koreanHandler.subscribe(dto.getCode());
                    koreanSubscribedStocks.add(dto.getCode());
                }catch (Exception e){
                    log.error("종목 {} 구독 실패: {}", dto.getCode(), e.getMessage());
                }
            }
        });
        int successCount = koreanSubscribedStocks.size() - beforeSize;
        log.info("장 시작 - 총 {} 종목 중 {} 종목 구독 완료",
                targetStocks.size(), successCount);
    }

    public void startOverseasMarketSubscription(){

        if (!isOverseasMarketOpen()) {
            log.info("해외 장이 열려있지 않아 자동 구독 스킵");
            return;
        }

        connectOverseasWebSocket();


        List<Stock> targetStocks = stockRepository.findByCurrencyName("USD");
        int beforeSize = overseasSubscribedStocks.size();

        targetStocks.forEach(stock -> {
            if (!overseasSubscribedStocks.contains(stock.getCode())) {
                try {
                    overseasHandler.subscribe(stock.getCode());
                    overseasSubscribedStocks.add(stock.getCode());
                }catch(Exception e){
                    log.error("종목 {} 구독 실패: {}", stock.getCode(), e.getMessage());
                }
            }
        });

        int successCount = overseasSubscribedStocks.size() - beforeSize;
        log.info("장 시작 - 총 {} 종목 중 {} 종목 구독 완료",
                targetStocks.size(), successCount);
    }

    @Override
    public MarketStatusResponse getMarketStatus() {
        // 국내 장 시간이면 "korean"
        if (isKoreanConnected.get() && isKoreanMarketOpen()) {
            return new MarketStatusResponse("korean");
        }

        // 해외 장 시간이면 "overseas"
        if (isOverseasConnected.get() && isOverseasMarketOpen()) {
            return new MarketStatusResponse("overseas");
        }

        // 둘 다 아니면 "no"
        return new MarketStatusResponse("no");
    }

    /* 스케줄링 - 장 마감 정리 */
    @Scheduled(cron = "0 20 15 * * MON-FRI", zone = "Asia/Seoul")
    public void cleanUpAfterKoreanMarketClose() {
        log.debug("국내 장 마감 - 마지막 데이터 저장 및 정리 시작");
        saveRemainingData("국내", koreanPendingData);
        disconnectKoreanWebSocket();
        log.info("국내 장 마감 정리 완료");
    }

    @Scheduled(cron = "0 0 16 * * MON-FRI", zone = "America/New_York")
    public void cleanUpAfterOverseasMarketClose() {
        log.debug("해외 장 마감 - 마지막 데이터 저장 및 정리 시작");
        saveRemainingData("해외", overseasPendingData);
        disconnectOverseasWebSocket();
        log.info("해외 장 마감 정리 완료");
    }

    private void saveRemainingData(String marketName, Map<String, LiveStockPriceStream> pendingData) {
        if (!pendingData.isEmpty()) {

            Map<String, LiveStockPriceStream> dataToSave = new HashMap<>(pendingData);
            pendingData.clear();

            dataToSave.values().forEach(dto -> {
                try {
                    liveStockPriceWebSocketSaverService.saveStockData(dto);
                } catch (Exception e) {
                    log.error("{} 종목 {} 마지막 저장 실패: {}", marketName, dto.code(), e.getMessage());
                }
            });

            log.info("{} 주식 마지막 배치 저장 완료 - 저장된 종목 수: {}", marketName, dataToSave.size());
        }
    }

    private void disconnectKoreanWebSocket() {
        if (!isKoreanConnected.get()) return;

        log.info("국내 WebSocket 연결 해제 시작");
        try {
            new ArrayList<>(koreanSubscribedStocks).forEach(code -> {
                try {
                    koreanStockUnsubscribe(code);
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
            koreanSubscribedStocks.clear();
            previousKoreanData.clear();
            koreanPendingData.clear();
        }

        log.info("국내 WebSocket 연결 해제 완료");
    }


    private void disconnectOverseasWebSocket() {
        if (!isOverseasConnected.get()) return;

        log.info("해외 WebSocket 연결 해제 시작");
        try {
            new ArrayList<>(overseasSubscribedStocks).forEach(code -> {
                try {
                    overseasStockUnsubscribe(code);
                } catch (Exception e) {
                    log.warn("해외 종목 {} 구독 해제 중 에러 발생: {}", code, e.getMessage());
                }
            });

            Thread.sleep(1000); //서버 처리 대기
            disconnectDBSession(); //db증권은 세션 정리를 하지 않을 경우 에러 발생함

        } catch (Exception e) {
            log.error("구독 해제 중 에러: {}", e.getMessage());
        } finally {
            overseasConnectionManager.stop();
            overseasSubscribedStocks.clear();
            previousOverseasData.clear();
            overseasPendingData.clear();
        }

        log.info("해외 WebSocket 연결 해제 완료");
    }


    public void disconnectDBSession(){
        try {
             //ERROR LettuceConnectionFactory has been STOPPED. Use start() to initialize it
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("authorization", "Bearer " + cachedAccessToken);

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

    /* 유틸리티 메서드 */
    private boolean isKoreanMarketOpen() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        boolean weekday = now.getDayOfWeek() != DayOfWeek.SATURDAY && now.getDayOfWeek() != DayOfWeek.SUNDAY;
        return  weekday
                && !now.toLocalTime().isBefore(LocalTime.of(9, 0))
                &&  now.toLocalTime().isBefore(LocalTime.of(15, 30));
    }

    private boolean isOverseasMarketOpen() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/New_York"));
        boolean weekday = now.getDayOfWeek() != DayOfWeek.SATURDAY && now.getDayOfWeek() != DayOfWeek.SUNDAY;
        return weekday
                && !now.toLocalTime().isBefore(LocalTime.of(9, 0))
                &&  now.toLocalTime().isBefore(LocalTime.of(16, 0));
    }

    @PreDestroy
    public void cleanUp() {
        log.info("애플리케이션 종료로 인한 WebSocket 연결 해제 시작");

        try {
            // 남은 데이터 저장
            saveRemainingData("국내", koreanPendingData);
            saveRemainingData("해외", overseasPendingData);

            // 연결 해제
            if (isKoreanConnected.get()) {
                disconnectKoreanWebSocket();
            }
            if (isOverseasConnected.get()) {
                disconnectOverseasWebSocket();
            }

            // 최종 리소스 정리 -> (안전장치)
            koreanSubscribedStocks.clear();
            overseasSubscribedStocks.clear();
            previousKoreanData.clear();
            previousOverseasData.clear();
            koreanPendingData.clear();
            overseasPendingData.clear();

        } catch (Exception e) {
            log.error("WebSocket cleanup 중 에러 발생", e);

            // 에러 발생해도 리소스는 강제 정리
            koreanSubscribedStocks.clear();
            overseasSubscribedStocks.clear();
            previousKoreanData.clear();
            previousOverseasData.clear();
            koreanPendingData.clear();
            overseasPendingData.clear();
        }

        log.info("WebSocket 연결 해제 완료");
    }



    /* 메트릭용 Getter 추가  */
    public Set<String> getKoreanSubscribedStocks() {
        return koreanSubscribedStocks;
    }

    public Set<String> getOverseasSubscribedStocks() {
        return overseasSubscribedStocks;
    }

    public boolean isKoreanConnected() {
        return isKoreanConnected.get();
    }

    public boolean isOverseasConnected() {
        return isOverseasConnected.get();
    }
}