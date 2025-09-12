package com.fintory.domain.point.dto;

import com.fintory.domain.point.model.PointTransaction;
import com.fintory.domain.point.model.PointTransactionSource;
import com.fintory.domain.point.model.PointTransactionType;

import java.time.LocalDateTime;

public record PointTransactionsResponse(

        int amount,
        PointTransactionType type,
        PointTransactionSource source,
        LocalDateTime createdAt

) {
    public static PointTransactionsResponse from(PointTransaction tx) {
        return new PointTransactionsResponse(
                tx.getAmount(),
                tx.getType(),
                tx.getSource(),
                tx.getCreatedAt()
        );
    }
}
