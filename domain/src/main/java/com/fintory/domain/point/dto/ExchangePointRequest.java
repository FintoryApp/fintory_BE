package com.fintory.domain.point.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ExchangePointRequest(

        @NotNull(message = "환전 포인트는 필수입니다.")
        @Min(value = 1, message = "환전 포인트는 1 이상이어야 합니다.")
        Integer point
) {
}
