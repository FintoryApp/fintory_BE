package com.fintory.domain.stock.dto.korean.response;

import com.fintory.domain.stock.model.LiveStockPrice;

import java.math.BigDecimal;

public record KoreanLiveStockPriceResponse(
        BigDecimal currentPrice,
        BigDecimal priceChange,
        BigDecimal priceChangeRate
) {
    public static KoreanLiveStockPriceResponse convertFromLiveStockPrice(LiveStockPrice liveStockPrice) {
        return new KoreanLiveStockPriceResponse(liveStockPrice.getCurrentPrice(), liveStockPrice.getPriceChange(), liveStockPrice.getPriceChangeRate());
    }
}
