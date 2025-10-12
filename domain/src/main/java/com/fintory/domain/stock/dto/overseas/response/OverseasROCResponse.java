package com.fintory.domain.stock.dto.overseas.response;

import java.math.BigDecimal;

public record OverseasROCResponse (
        String stockCode,
        String stockName,
        BigDecimal closePrice,
        BigDecimal openPrice,
        String profileImageUrl

){
}
