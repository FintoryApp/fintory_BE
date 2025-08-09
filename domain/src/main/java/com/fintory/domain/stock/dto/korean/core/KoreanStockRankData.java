package com.fintory.domain.stock.dto.korean.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record KoreanStockRankData(
        @JsonProperty("hts_avls") BigDecimal marketCap,
        @JsonProperty("acml_vol") Long  tradingVolume,
        @JsonProperty("prdy_ctrt") BigDecimal roc
) {
}
