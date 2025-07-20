package com.fintory.child.stock.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OverseasStockRealPrice {


    private String code;

    private String time;

    private int currentPrice;

    private BigDecimal priceChange;

    private BigDecimal priceChangeRate;


}
