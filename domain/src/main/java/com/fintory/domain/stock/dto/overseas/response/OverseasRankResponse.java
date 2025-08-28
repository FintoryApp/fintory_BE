package com.fintory.domain.stock.dto.overseas.response;

import java.math.BigDecimal;

public record OverseasRankResponse (
        String stockName,
        String stockCode,
        int rank,
        String profileImageUrl,
        BigDecimal currentPrice,
        BigDecimal priceChange,
        BigDecimal priceChangeRate
){
    public OverseasRankResponse(String stockCode, String stockName, int rank,BigDecimal currentPrice, BigDecimal priceChange, BigDecimal priceChangeRate) {
        this(stockCode, stockName, rank,null,currentPrice,priceChange,priceChangeRate);
    }
}
