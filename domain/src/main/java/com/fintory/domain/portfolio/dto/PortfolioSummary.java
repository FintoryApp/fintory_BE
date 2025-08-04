package com.fintory.domain.portfolio.dto;

import java.math.BigDecimal;


public record PortfolioSummary (
     BigDecimal totalReturnRate,
     BigDecimal totalEvaluationAmount,
     BigDecimal totalPurchasePrice,
     BigDecimal totalMoney
){}
