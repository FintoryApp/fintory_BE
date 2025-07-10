package com.fintory.child.consulting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentSummary {
    private int totalInvestmentsCount; //투자횟수
    private BigDecimal totalReturnRate; // 총 수익률

}
