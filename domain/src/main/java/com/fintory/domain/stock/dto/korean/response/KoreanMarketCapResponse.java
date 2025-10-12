package com.fintory.domain.stock.dto.korean.response;

import java.math.BigDecimal;


public record KoreanMarketCapResponse(
        String stockCode,
        String stockName,
        BigDecimal marketCap,
        BigDecimal currentPrice,
        String companyImageUrl
) {
}
