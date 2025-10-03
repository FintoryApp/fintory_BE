package com.fintory.domain.alarm.dto;

import java.math.BigDecimal;

public record PriceAlertRequest(
         String stockCode,
         BigDecimal targetPrice
){
}
