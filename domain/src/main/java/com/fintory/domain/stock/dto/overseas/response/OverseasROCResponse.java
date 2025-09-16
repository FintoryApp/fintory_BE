package com.fintory.domain.stock.dto.overseas.response;

import java.math.BigDecimal;

public record OverseasROCResponse (
        String stockCode,
        String stockName,
        BigDecimal closePrice,
        String profileImageUrl

){
    public OverseasROCResponse(String stockCode, String stockName, BigDecimal closePrice){
        this(stockCode,stockName,closePrice,null);
    }
}
