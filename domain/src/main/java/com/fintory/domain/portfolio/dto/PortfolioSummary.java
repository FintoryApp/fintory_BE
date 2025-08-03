package com.fintory.domain.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioSummary {
    private BigDecimal totalReturnRate;
    private BigDecimal totalEvaluationAmount;
    private BigDecimal totalPurchasePrice;
    private BigDecimal totalMoney;
}
