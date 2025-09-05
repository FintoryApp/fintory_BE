package com.fintory.domain.stock.dto.korean.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KoreanLiveStockPrice(

        @JsonProperty("stck_prpr") BigDecimal currentPrice,
        @JsonProperty("prdy_vrss") BigDecimal priceChange,
        @JsonProperty("prdy_ctrt") BigDecimal priceChangeRate

){}
