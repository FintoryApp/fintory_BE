package com.fintory.domain.stock.dto.korean.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintory.domain.stock.dto.korean.core.KoreanStockPriceHistory;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KoreanStockPriceHistoryWrapper(
        @JsonProperty("output2") List<KoreanStockPriceHistory> output
){}
