package com.fintory.domain.stock.service;


import com.fintory.domain.stock.dto.StockSearchRequest;
import com.fintory.domain.stock.dto.StockSearchResponse;

import java.util.List;

public interface SearchStockService {

    /**
     *
     * 검색어에 해당하는 code, name, eng_name을 가진 Stock List를 반환하는 메소드
     * @param keyword
     * @return 주식 이름, 코드(심볼)으로 구성된 Stock List
     */
    public List<StockSearchResponse> searchStock(StockSearchRequest keyword);
}
