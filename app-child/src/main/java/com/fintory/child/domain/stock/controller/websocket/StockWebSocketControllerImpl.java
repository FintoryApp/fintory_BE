package com.fintory.child.domain.stock.controller.websocket;

import com.fintory.domain.stock.dto.websocket.StockMessageRequest;
import com.fintory.domain.stock.dto.websocket.StockSubscriptionRequest;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.service.websocket.LiveStockPriceWebsocketService;
import com.fintory.infra.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


@Controller //Http ResponseBody가 아닌 WebSocket 메시지 브로드 캐스트용
@RequiredArgsConstructor
@Slf4j
public class StockWebSocketControllerImpl {

    private final LiveStockPriceWebsocketService liveStockWebSocketService;
    private final StockRepository stockRepository;

    /**
     * 클라이언트로부터 STOMP 구독/해제 요청을 받아 처리하는 단일 엔드포인트
     *
     * @param request 구독/해제할 종목 코드 목록과 요청 타입("subscribe" 또는 "unsubscribe")을 담은 요청
     */
    @MessageMapping("/stock/subscription") // /app이 자동으로 추가
    @SendTo("/topic/stock/subscription") // /topic이 있어야 브로커가 인식
    public void handleStockSubscription(@RequestBody StockSubscriptionRequest request) {
        if ("subscribe".equalsIgnoreCase(request.type())) {
            log.info("STOMP 구독 요청: {}", request.codes());
            liveStockWebSocketService.subscribe(request.codes());
        } else if ("unsubscribe".equalsIgnoreCase(request.type())) {
            log.info("STOMP 구독 해제 요청: {}", request.codes());
            liveStockWebSocketService.unsubscribe(request.codes());
        } else {
            log.warn("알 수 없는 STOMP 요청 타입: {}", request.type());
        }
    }

    /**
     * 프론트엔드에서 STOMP로 한국 주식 구독 요청
     *
     * @param request 종목 코드가 포함된 요청
     */
    @MessageMapping("/stock/subscribe/korean")
    public void subscribeKoreanStock(StockMessageRequest request) {
            log.info("STOMP를 통한 한국 종목 {} 구독 요청", request.code());
            // 실제 WebSocket 구독 시작 (자동 연결 포함)
            liveStockWebSocketService.koreanStockSubscribe(request.code());
    }

    /**
     * 프론트엔드에서 STOMP로 한국 주식 구독 해제 요청
     */
    @MessageMapping("/stock/unsubscribe/korean")
    public void unsubscribeKoreanStock(StockMessageRequest request) {
            log.info("STOMP를 통한 한국 종목 {} 구독 해제 요청", request.code());
            liveStockWebSocketService.koreanStockUnsubscribe(request.code());
    }

    /**
     * 프론트엔드에서 STOMP로 해외 주식 구독 요청
     */
    @MessageMapping("/stock/subscribe/overseas")
    public void subscribeOverseasStock(StockMessageRequest request) {
            log.info("STOMP를 통한 해외 종목 {} 구독 요청", request.code());
            liveStockWebSocketService.overseasStockSubscribe(request.code());
    }

    /**
     * 프론트엔드에서 STOMP로 해외 주식 구독 해제 요청
     */
    @MessageMapping("/stock/unsubscribe/overseas")
    public void unsubscribeOverseasStock(StockMessageRequest request) {
            log.info("STOMP를 통한 해외 종목 {} 구독 해제 요청", request.code());
            liveStockWebSocketService.overseasStockUnsubscribe(request.code());
    }

    //NOTE 프론트에서 어떻게 구현할지 정해지지 않아서 현재처럼 전체 구독 + 개별 구독을 같이 구현
    /**
     * 전체 구독
     *
     */
    @MessageMapping("/stock/subscribe-all/korean")
    @Async
    public void subscribeKoreanStockAll() {
        List<Stock> stockList = stockRepository.findByCurrencyName("KRW");
        for (Stock stock : stockList) {
                log.info("STOMP를 통한 국내 종목 {} 구독 요청", stock.getCode());
                // 실제 WebSocket 구독 시작 (자동 연결 포함)
                liveStockWebSocketService.koreanStockSubscribe(stock.getCode());
        }
        log.info("국내 종목 일괄 구독 완료");

    }

    @MessageMapping("/stock/unsubscribe-all/korean")
    @Async
    public void unsubscribeKoreanStockAll() {
        List<Stock> stockList = stockRepository.findByCurrencyName("KRW");
        for (Stock stock : stockList) {
                log.info("STOMP를 통한 국내 종목 {} 구독 취소 요청", stock.getCode());
                // 실제 WebSocket 구독 시작 (자동 연결 포함)
                liveStockWebSocketService.koreanStockUnsubscribe(stock.getCode());
        }
        log.info("국내 종목 일괄 구독 취소 완료");

    }

    @MessageMapping("/stock/subscribe-all/overseas")
    @Async
    public void subscribeOverseasStockAll() {
        List<Stock> stockList = stockRepository.findByCurrencyName("USD");
        for (Stock stock : stockList) {
            log.info("STOMP를 통한 해외 종목 {} 구독 요청", stock.getCode());
            // 실제 WebSocket 구독 시작 (자동 연결 포함)
            liveStockWebSocketService.overseasStockSubscribe(stock.getCode());
        }
        log.info("해외 종목 일괄 구독 완료");

    }

    @MessageMapping("/stock/unsubscribe-all/overseas")
    @Async
    public void unsubscribeOverseasStockAll() {
        List<Stock> stockList = stockRepository.findByCurrencyName("USD");
        for (Stock stock : stockList) {
                log.info("STOMP를 통한 해외 종목 {} 구독 취소 요청", stock.getCode());
                // 실제 WebSocket 구독 시작 (자동 연결 포함)
                liveStockWebSocketService.overseasStockUnsubscribe(stock.getCode());
        }
        log.info("해외 종목 일괄 구독 취소 완료");
    }

}