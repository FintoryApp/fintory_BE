package com.fintory.domain.stock.dto.korean.response;

import com.fintory.domain.stock.model.LiveStockPrice;

import java.math.BigDecimal;

public record KoreanLiveStockPriceResponse(
        BigDecimal currentPrice,
        BigDecimal openPrice
) {
    public static KoreanLiveStockPriceResponse convertFromLiveStockPrice(LiveStockPrice liveStockPrice,BigDecimal openPrice) {
        return new KoreanLiveStockPriceResponse(liveStockPrice.getCurrentPrice(),openPrice);
    }
}
