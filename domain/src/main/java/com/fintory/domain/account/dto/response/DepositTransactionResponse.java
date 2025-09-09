package com.fintory.domain.account.dto.response;

import com.fintory.domain.account.model.DepositTransaction;
import com.fintory.domain.account.model.DepositTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DepositTransactionResponse(
        BigDecimal amount,
        String description,
        DepositTransactionType type,
        LocalDateTime occurredAt
) {
    public static DepositTransactionResponse from(DepositTransaction entity) {
        return new DepositTransactionResponse(
                entity.getAmount(),
                entity.getDescription(),
                entity.getType(),
                entity.getOccurredAt()
        );
    }
}
