package com.fintory.domain.stock.dto.korean.response;

import java.math.BigDecimal;

public record KoreanRankResponse(
        String stockCode,
        String stockName,
        int rank,
        String profileImageUrl,
        BigDecimal currentPrice,
        BigDecimal priceChange,
        BigDecimal priceChangeRate
){
    public KoreanRankResponse(String stockCode, String stockName, int rank,BigDecimal currentPrice, BigDecimal priceChange, BigDecimal priceChangeRate) {
        this(stockCode, stockName, rank,null,currentPrice,priceChange,priceChangeRate);
    }
}
