package com.fintory.domain.stock.service;

import com.fintory.domain.stock.dto.IntraDayResponse;
import com.fintory.domain.stock.dto.StockPriceHistoryWrapper;
import com.fintory.domain.stock.model.IntervalType;
import com.fintory.domain.stock.model.Stock;

import java.util.List;

public interface StockPriceHistoryService {

    /**
     *
     * MarketStack API를 통해 모든 Interval(weekly,monthly,yearly,fiveyearly,total)에 해당하는 시세 데이터를 StockPriceHistory에 저장하는 메소드
     *
     */
     void getAllIntraDay();

    /**
     *
     * 기간별 시세를 저장하는 템플릿 메소드
     *
     * @param dataList MarketStack API로부터 받은 기간별 시세 데이터
     * @param stock 주식
     * @param intervalType weekly, monthly, yearly, fiveyearly, total
     */
     void updateIntervalData(List<IntraDayResponse> dataList, Stock stock, IntervalType intervalType);

}
