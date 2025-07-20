package com.fintory.child.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverseasStockMarketCapTop20 {
    private String code;
    private BigDecimal marketCap;
    private OverseasStockRealPrice overseasStockRealPrice;

}
