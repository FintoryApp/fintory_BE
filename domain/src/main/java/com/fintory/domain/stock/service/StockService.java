package com.fintory.domain.stock.service;

import com.fintory.domain.stock.dto.*;

import java.util.List;

public interface StockService {

    /**
     * 시가 총액 순위 조회 메소드
     * @param currency 국내/해외
     * @return 주식의 이름, code, 회사 이미지 url, 현재가, 변화 가격, 변화률, 시가총액 순위 제공
     */
    List<RankResponse> getMarketCapTop20(String currency);


    /**
     * 등락률 순위 조회 메소드
     * @param currency 국내/해외
     * @return 주식의 이름, code, 회사 이미지 url, 현재가, 변화 가격, 변화률, 등락률 순위 제공
     */
    List<RankResponse> getROCTop20(String  currency);

    /**
     * 거래량 순위 조회 메서드
     * @param currency 국내/해외
     * @return 주식의 이름, code, 현재가, 회사 이미지 url, 변화 가격, 변화률, 등락률 순위 제공
     */
    List<RankResponse> getTradingVolumeTop20(String currency);


    /**
     * 주식 종목 검색 기능 메소드
     * @param stockSearchRequest keyword를 다음 객체
     * @return 이름(영어이름 포함),코드에 keyword를 포함하고 있는 주식 이름과 종목 코드 리스트 반환
     */
    List<StockSearchResponse> searchStock(StockSearchRequest stockSearchRequest);

    /**
     * DB에서 기간별 시세 데이터 조회 메소드
     * @param code 주식 종목 코드
     * @return 주식의 코드,이름과 함께 각 IntervalType과 시세 데이터 리스트를 담은 Map 반환
     */
    StockPriceHistoryWrapper getOverseasStockPriceHistory(String code);

    /**
     * DB에서 현재가 데이터 조회 메소드
     * @param code 주식 종목 코드
     * @return 주식 종목 이름, 현재가, 변화 가격, 변화률 반환
     */
    LiveStockPriceResponse getEachLiveStockPrice(String code);
}
