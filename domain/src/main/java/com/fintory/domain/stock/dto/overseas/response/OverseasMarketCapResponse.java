package com.fintory.domain.stock.dto.overseas.response;

import java.math.BigDecimal;

public record OverseasMarketCapResponse (
        String stockCode,
        String stockName,
        BigDecimal marketCap,
        String profileImageUrl
) {

    //TODO profileImageURL
    public OverseasMarketCapResponse(String stockCode, String stockName, BigDecimal marketCap) {
        this(stockCode, stockName, marketCap,null);
    }
}

