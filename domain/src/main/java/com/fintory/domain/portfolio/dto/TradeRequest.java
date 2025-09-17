package com.fintory.domain.portfolio.dto;

import com.fintory.domain.portfolio.model.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TradeRequest(
        @NotBlank(message="주식 코드는 필수입니다")
        String stockCode,

        @NotNull(message="수량은 필수입니다")
        @DecimalMin(value="0.001",message="수량은 0.001 이상이어야 합니다")
        @Digits(integer=10, fraction = 3, message = "수량은 소수점 셋째 자리까지만 허용됩니다.")
        BigDecimal quantity,

        @NotNull(message="현재가는 필수입니다")
        @DecimalMin(value = "0.01", message = "가격은 0.01 이상이어야 합니다")
        BigDecimal price,

        @NotNull(message="거래 유형은 필수입니다")
        TransactionType transactionType
){}
