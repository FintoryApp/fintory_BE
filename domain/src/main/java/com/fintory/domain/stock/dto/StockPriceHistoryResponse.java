package com.fintory.domain.stock.dto;

import com.fintory.domain.stock.model.IntervalType;

import java.math.BigDecimal;

public record StockPriceHistoryResponse(
         BigDecimal openPrice,

         BigDecimal closePrice,

         BigDecimal highPrice,

         BigDecimal lowPrice,

         IntervalType intervalType,

         String date

) {
}
