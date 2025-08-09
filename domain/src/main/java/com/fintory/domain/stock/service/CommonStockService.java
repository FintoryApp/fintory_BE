package com.fintory.domain.stock.service;


import com.fintory.domain.stock.dto.korean.response.StockSearchResponse;

import java.util.List;

public interface CommonStockService {
    /**
     * 주식 종목 키워드를(주식 코드/ 주식 종목 이름) 검색했을 때 나오는 리스트 조회
     * @param keyword
     * @return 해당 키워드를 가진 주식 종목 리스트
     */
    public List<StockSearchResponse> searchStock(String keyword);
}
