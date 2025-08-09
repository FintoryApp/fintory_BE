package com.fintory.domain.stock.dto.overseas.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record OverseasStockRankData(
        @JsonProperty("tomv")BigDecimal marketCap,
        @JsonProperty("pvol") Long tradingVolume,
        @JsonProperty("p_xrat") BigDecimal roc
        ){ }
