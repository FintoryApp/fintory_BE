package com.fintory.domain.stock.dto.websocket;

import java.math.BigDecimal;

public record LiveStockPriceResponse(
        BigDecimal currentPrice,
        BigDecimal priceChange,
        BigDecimal priceChangeRate,
        String stockName
) {
}
