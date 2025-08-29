package com.fintory.domain.portfolio.dto;

import com.fintory.domain.portfolio.model.TransactionType;

import java.math.BigDecimal;

public record TradeRequest(
        String stockCode,
        Integer quantity,
        BigDecimal price,
        TransactionType transactionType
){}
