package com.fintory.domain.stock.dto.korean.response;

import java.math.BigDecimal;

public record KoreanROCResponse (
        String stockCode,
        String stockName,
        BigDecimal closePrice,
        String profileImageUrl

){
    public KoreanROCResponse(String stockCode, String stockName, BigDecimal closePrice){
        this(stockCode,stockName,closePrice,null);
    }
}
