package com.fintory.domain.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnedStockList {
    private BigDecimal evaluationAmount;
    private BigDecimal profit;
    private BigDecimal returnRate;
    private String stockName;
    private String stockCode;
}
