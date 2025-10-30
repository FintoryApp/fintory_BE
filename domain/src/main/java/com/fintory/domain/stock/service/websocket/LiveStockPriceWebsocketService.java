package com.fintory.domain.stock.service.websocket;

import com.fintory.domain.stock.dto.websocket.MarketStatusResponse;

import java.util.Map;
import java.util.Set;

/**
 * 실시간 주식 가격 WebSocket 서비스 인터페이스
 * 한국 및 해외 주식의 실시간 가격 구독/해제 및 데이터 전송을 담당
 */
public interface LiveStockPriceWebsocketService {

    /**
     * 한국 주식 종목 구독
     * @param code 주식 종목 코드
     * @throws com.fintory.common.exception.DomainException 구독 실패 시
     */
    void koreanStockSubscribe(String code);

    /**
     * 한국 주식 종목 구독 해제
     * @param code 주식 종목 코드
     * @throws com.fintory.common.exception.DomainException 구독 해제 실패 시
     */
    void koreanStockUnsubscribe(String code);

    /**
     * 해외 주식 종목 구독
     * @param code 주식 종목 코드
     * @throws com.fintory.common.exception.DomainException 구독 실패 시
     */
    void overseasStockSubscribe(String code);

    /**
     * 해외 주식 종목 구독 해제
     * @param code 주식 종목 코드
     * @throws com.fintory.common.exception.DomainException 구독 해제 실패 시
     */
    void overseasStockUnsubscribe(String code);

    /**
     * 해당 토픽을 구독한 사용자(프론트)들에게 주식 데이터 브로드캐스트
     * @param stockCode 주식 종목 코드
     * @param stockData 전송할 주식 데이터 (KoreanLiveStockPriceStream 또는 OverseasLiveStockPriceStream)
     */
    void sendStockData(String stockCode, Object stockData);


    /**
     * stomp 구독 시 어떤 장이 열린건지 확인
     * @return korean, overseas, no 중 하나
     */
    MarketStatusResponse getMarketStatus();


    /* 매트릭용 Getter 함수 추가 */
    Set<String> getKoreanSubscribedStocks();
    Set<String> getOverseasSubscribedStocks();
    boolean isKoreanConnected();
    boolean isOverseasConnected();
}