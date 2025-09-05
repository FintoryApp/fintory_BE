package com.fintory.domain.portfolio.dto;

import com.fintory.domain.portfolio.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TradeRequest(
        @NotBlank(message="주식 코드는 필수입니다")
        String stockCode,

        @NotNull(message="수량은 필수입니다")
        @Min(value=1,message="수량은 1이상이어야 합니다")
        Integer quantity,

        @NotNull(message="현재가는 필수입니다")
        @DecimalMin(value = "0.01", message = "가격은 0.01 이상이어야 합니다")
        BigDecimal price,

        @NotNull(message="거래 유형은 필수입니다")
        TransactionType transactionType
){}
