package com.fintory.domain.portfolio.dto;

import com.fintory.domain.portfolio.model.MarketType;

import java.math.BigDecimal;

public record TradeCalculation(
        BigDecimal amount,
        MarketType marketType
) {
}
