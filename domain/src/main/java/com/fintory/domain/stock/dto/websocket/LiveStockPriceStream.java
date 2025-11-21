package com.fintory.domain.stock.dto.websocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LiveStockPriceStream(
        String code,
        BigDecimal currentPrice,
        BigDecimal priceChange,
        BigDecimal priceChangeRate
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
