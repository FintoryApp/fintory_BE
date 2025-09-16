package com.fintory.domain.stock.dto.korean.response;

import java.math.BigDecimal;

public record KoreanROCResponse (
        String stockCode,
        String stockName,
        BigDecimal closePrice

){
}
