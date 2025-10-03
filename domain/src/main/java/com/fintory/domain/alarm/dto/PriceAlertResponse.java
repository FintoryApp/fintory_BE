package com.fintory.domain.alarm.dto;

import java.math.BigDecimal;

public record PriceAlertResponse(
        Long id,
        BigDecimal targetPrice
) {
}
