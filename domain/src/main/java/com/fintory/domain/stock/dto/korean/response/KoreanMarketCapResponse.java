package com.fintory.domain.stock.dto.korean.response;

import java.math.BigDecimal;


public record KoreanMarketCapResponse(
        String stockCode,
        String stockName,
        BigDecimal marketCap,
        BigDecimal currentPrice,
        String profileImageUrl
) {

    //TODO profileImageURL
    public KoreanMarketCapResponse(String stockCode, String stockName, BigDecimal marketCap,BigDecimal currentPrice) {
        this(stockCode, stockName, marketCap, currentPrice,null);
    }
}
