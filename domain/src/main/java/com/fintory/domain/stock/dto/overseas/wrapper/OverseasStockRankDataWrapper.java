package com.fintory.domain.stock.dto.overseas.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintory.domain.stock.dto.overseas.core.OverseasStockRankData;


@JsonIgnoreProperties(ignoreUnknown = true)
public record OverseasStockRankDataWrapper(
    @JsonProperty("output") OverseasStockRankData output
){}
