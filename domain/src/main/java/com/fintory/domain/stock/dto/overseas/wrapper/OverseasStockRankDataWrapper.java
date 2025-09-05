package com.fintory.domain.stock.dto.overseas.wrapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintory.domain.stock.dto.overseas.core.OverseasStockRankData;

public record OverseasStockRankDataWrapper(
    @JsonProperty("output") OverseasStockRankData output
){}
