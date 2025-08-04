package com.fintory.domain.portfolio.dto;


import java.math.BigDecimal;



public record OwnedStockList (
    String stockCode,
    String stockName,
    BigDecimal evaluationAmount,
    BigDecimal profit,
    BigDecimal returnRate
){}
