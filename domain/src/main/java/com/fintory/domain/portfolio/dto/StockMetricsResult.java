package com.fintory.domain.portfolio.dto;

import java.math.BigDecimal;

public record StockMetricsResult(
        BigDecimal avgPurchasePrice,
        int currentQuantity,
        BigDecimal totalInvestment
) {
}
