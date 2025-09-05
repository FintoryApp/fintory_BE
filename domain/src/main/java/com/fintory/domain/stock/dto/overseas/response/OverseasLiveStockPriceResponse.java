package com.fintory.domain.stock.dto.overseas.response;

import com.fintory.domain.stock.model.LiveStockPrice;

import java.math.BigDecimal;

public record OverseasLiveStockPriceResponse(
        BigDecimal currentPrice,
        BigDecimal priceChange,
        BigDecimal priceChangeRate
) {
    public static OverseasLiveStockPriceResponse convertFromLiveStockPrice(LiveStockPrice liveStockPrice) {
        return new OverseasLiveStockPriceResponse(liveStockPrice.getCurrentPrice(), liveStockPrice.getPriceChange(), liveStockPrice.getPriceChangeRate());
    }
}
