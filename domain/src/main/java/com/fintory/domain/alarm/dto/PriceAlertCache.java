package com.fintory.domain.alarm.dto;

import com.fintory.domain.alarm.model.PriceAlert;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PriceAlertCache implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long childId;
    private String stockCode;
    private String stockName;
    private BigDecimal targetPrice;

    public static PriceAlertCache from(PriceAlert priceAlert) {
        return new PriceAlertCache(
                priceAlert.getId(),
                priceAlert.getChild().getId(),
                priceAlert.getStock().getCode(),
                priceAlert.getStock().getName(),
                priceAlert.getTargetPrice()
        );
    }
}