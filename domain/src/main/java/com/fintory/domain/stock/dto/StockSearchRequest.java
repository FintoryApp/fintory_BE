package com.fintory.domain.stock.dto;

import org.hibernate.validator.constraints.NotBlank;

public record StockSearchRequest(
        @NotBlank(message="키워드를 입력해주세요")
        String keyword
) {
}
