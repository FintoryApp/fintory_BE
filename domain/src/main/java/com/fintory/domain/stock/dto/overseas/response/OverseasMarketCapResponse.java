package com.fintory.domain.stock.dto.overseas.response;

import java.math.BigDecimal;

public record OverseasMarketCapResponse (
        String stockCode,
        String stockName,
        BigDecimal marketCap,
        BigDecimal currentPrice,
        String companyImageUrl
) {
}

