package com.fintory.domain.portfolio.dto;

import java.math.BigDecimal;

public record ExchangeRateResponse(
        BigDecimal exchangeRate
) {
}
