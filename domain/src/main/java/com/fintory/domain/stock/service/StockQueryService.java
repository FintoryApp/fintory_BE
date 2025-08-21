package com.fintory.domain.stock.service;

import com.fintory.domain.stock.dto.LiveStockPriceResponse;
import com.fintory.domain.stock.dto.OrderBookResponse;
import com.fintory.domain.stock.dto.StockPriceHistoryWrapper;
import com.fintory.domain.stock.model.Stock;

import java.util.List;

public interface StockQueryService {
    /**
     * DB에서 기간별 시세 조회하는 메소드
     * @param stock 기간별 시세를 얻고자 하는 주식 종목
     * @return 각각의 Interval_type에 해당하는(weekly, monthly, yearly, fiveyearly, total) 시세 데이터
     */
    StockPriceHistoryWrapper getStockPriceHistory(Stock stock);


    /**
     * DB에서 개별 주식 종목의
     * @param stock 현재가 데이터를 얻고자 하는 주식 종목
     * @return 각 주식 종목의 현재가, 가격 변화, 변화율 데이터를 모아놓은 리스트
     */
    LiveStockPriceResponse getEachLiveStockPrice(Stock stock);


    //NOTE 현재는 컨트롤러에서 사용x -> 필요한 경우가 생길 때 사용 예쩡
    /**
     * DB에서 요청받은 주식 종목 리스트의 현재가 데이터를 조회하는 메소드
     * @param stockList 현재가 데이터를 얻고자 하는 주식 종목 리스트
     * @return 현재가, 가격 변화, 변화율 데이터
     */
    List<LiveStockPriceResponse> getEachLiveStockPrice(List<Stock> stockList);
}
