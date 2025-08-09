package com.fintory.domain.stock.dto.korean.wrapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintory.domain.stock.dto.korean.core.KoreanStockPriceHistory;

import java.util.List;

public record KoreanStockPriceHistoryWrapper(
        @JsonProperty("output2") List<KoreanStockPriceHistory> output
){}
