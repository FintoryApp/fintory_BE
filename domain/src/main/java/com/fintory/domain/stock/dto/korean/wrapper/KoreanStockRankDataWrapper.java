package com.fintory.domain.stock.dto.korean.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintory.domain.stock.dto.korean.core.KoreanStockRankData;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KoreanStockRankDataWrapper(
        @JsonProperty("output") KoreanStockRankData output
){

}
