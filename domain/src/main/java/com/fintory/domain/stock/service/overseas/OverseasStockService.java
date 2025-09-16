package com.fintory.domain.stock.service.overseas;


import com.fintory.domain.stock.dto.overseas.response.*;

import java.util.List;

public interface OverseasStockService {



    /**
     * 해외 주식 시가총액 Top20 조회
     * @return 시가총액 순위 리스트
     */
    List<OverseasMarketCapResponse> getOverseasMarketCapTop20();

    /**
     * 해외 주식 등락률 Top20 조회
     * @return 등락률 순위 리스트
     */
    List<OverseasROCResponse> getOverseasROCTop20();

    /**
     * 해외 주식 거래량 Top20 조회
     * @return 거래량 순위 리스트
     */
    List<OverseasRankResponse> getOverseasTradingVolumeTop20();


    /**
     * 해외 주식 가격 이력 조회
     * @param code 주식 코드
     * @return 가격 이력 정보
     */
    OverseasStockPriceHistoryResponse getOverseasStockPriceHistory(String code);


    /**
     * 해외 주식 현재가 데이터 조회
     * @param code 주식 종목 코드
     * @return 현재가,변화 가격,변화률 반환
     */
    OverseasLiveStockPriceResponse getLiveStockPrice(String code);
}