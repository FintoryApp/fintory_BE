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
public class KoreanStockROCTop20 {

    private String code;
    private BigDecimal riseRate;
    private KoreanStockRealPrice koreanStockRealPrice;
}
