package com.fintory.domain.stock.dto.overseas.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OverseasLiveStockPrice(
    @JsonProperty("last") BigDecimal currentPrice,
    @JsonProperty("base") BigDecimal base
) {
}
