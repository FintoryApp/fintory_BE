package com.fintory.domain.stock.dto;

import java.math.BigDecimal;

public record RankResponse(
        String stockCode,
        String stockName,
        int rank,
        String profileImageUrl,
        BigDecimal currentPrice,
        BigDecimal priceChange,
        BigDecimal priceChangeRate
){
    public RankResponse(String stockCode, String stockName, int rank, BigDecimal currentPrice, BigDecimal priceChange, BigDecimal priceChangeRate) {
        this(stockCode, stockName, rank,null,currentPrice,priceChange,priceChangeRate);
    }
}
