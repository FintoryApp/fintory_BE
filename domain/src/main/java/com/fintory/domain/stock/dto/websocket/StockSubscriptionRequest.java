package com.fintory.domain.stock.dto.websocket;

import java.util.List;

public record StockSubscriptionRequest(
        String type,
        List<String> codes
) {
}
