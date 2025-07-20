package com.fintory.child.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OverseasStockROCTop20 {
    private String code;
    private BigDecimal riseRate;
    private OverseasStockRealPrice overseasStockRealPrice;
}
