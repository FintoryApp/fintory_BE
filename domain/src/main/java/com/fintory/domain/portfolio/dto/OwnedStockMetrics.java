package com.fintory.domain.portfolio.dto;


import java.math.BigDecimal;
import java.util.List;

public record OwnedStockMetrics(
        String stockCode,
        String stockName,
        BigDecimal avgPurchasePrice,
        int currentQuantity,
        List<StockTransactionInfo> stockTransactionInfoList
){}
