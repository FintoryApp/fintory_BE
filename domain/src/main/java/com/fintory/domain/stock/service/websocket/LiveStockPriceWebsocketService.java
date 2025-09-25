package com.fintory.domain.stock.service.websocket;

import java.util.List;

/**
 * 실시간 주식 가격 WebSocket 서비스 인터페이스
 * 한국 및 해외 주식의 실시간 가격 구독/해제 및 데이터 전송을 담당
 */
public interface LiveStockPriceWebsocketService {

    void subscribe(List<String> codes);

    void unsubscribe(List<String> codes);

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


}