package com.fintory.domain.portfolio.dto;

import java.math.BigDecimal;


public record PortfolioSummary (
     BigDecimal totalPurchasePrice,
     BigDecimal totalMoney
){}
