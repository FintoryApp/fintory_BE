package com.fintory.domain.stock.dto.overseas.response;

import com.fintory.domain.stock.model.LiveStockPrice;

import java.math.BigDecimal;

public record OverseasLiveStockPriceResponse(
        BigDecimal currentPrice,
        BigDecimal openPrice
) {
    public static OverseasLiveStockPriceResponse convertFromLiveStockPrice(BigDecimal closePrice,BigDecimal openPrice) {
        return new OverseasLiveStockPriceResponse(closePrice,openPrice);
    }
}
