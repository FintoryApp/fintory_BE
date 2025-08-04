package com.fintory.domain.portfolio.dto;

import com.fintory.domain.portfolio.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StockTransactionInfo(
        BigDecimal pricePerShare,
        int quantity,
        BigDecimal exchangeRate,
        TransactionType transactionType,
        LocalDateTime executedAt
) {
}
