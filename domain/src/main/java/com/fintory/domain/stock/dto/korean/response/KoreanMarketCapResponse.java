package com.fintory.domain.stock.dto.korean.response;

import java.math.BigDecimal;


public record KoreanMarketCapResponse(
        String stockCode,
        String stockName,
        BigDecimal marketCap,
        String profileImageUrl
) {

    //TODO profileImageURL
    public KoreanMarketCapResponse(String stockCode, String stockName, BigDecimal marketCap) {
        this(stockCode, stockName, marketCap,null);
    }
}
