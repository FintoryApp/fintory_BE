package com.fintory.domain.stock.dto.overseas.response;

import java.math.BigDecimal;

public record OverseasROCResponse (
        String stockCode,
        String stockName,
        BigDecimal closePrice,
        BigDecimal openPrice,
        String profileImageUrl

){
    public OverseasROCResponse(String stockCode, String stockName, BigDecimal closePrice,BigDecimal openPrice){
        this(stockCode,stockName,closePrice,openPrice,null);
    }
}
