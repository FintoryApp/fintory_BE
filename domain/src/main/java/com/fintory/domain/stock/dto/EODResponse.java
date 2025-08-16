package com.fintory.domain.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record EODResponse(
        @JsonProperty("open")BigDecimal open,
        @JsonProperty("high")BigDecimal high,
        @JsonProperty("low") BigDecimal low,
        @JsonProperty("close") BigDecimal close,
        @JsonProperty("volume") double volume,
        @JsonProperty("symbol") String symbol

        ) {
 }
