package com.fintory.domain.stock.dto.korean.wrapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintory.domain.stock.dto.korean.core.KoreanStockRankData;

public record KoreanStockRankDataWrapper(
        @JsonProperty("output") KoreanStockRankData output
){

}
