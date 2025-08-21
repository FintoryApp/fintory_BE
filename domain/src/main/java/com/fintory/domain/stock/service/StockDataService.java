package com.fintory.domain.stock.service;

/*
*
* */
public interface StockDataService {

    /**
     *
     * MarketStack API로부터 장 마감 후의 데이터를 받아서
     * LiveStockPrice, StockRank를 저장하는 통합 메소드
     *
     */
    public void getAllEOD();

    /**
     *
     * MarketStack API로부터 받은 데이터를 기반으로
     * 전체 순위를 초기화하는 메소드
     */
    public void saveAllRank();
}
