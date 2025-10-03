package com.fintory.domain.account.model;


import com.fintory.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name="deposit_transaction")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DepositTransaction extends BaseEntity {

    // 포인트 -> 현금(-), 주식 매수 -> 현금(-), 주식 매도 -> 현금(+), 기본금 입금 -> 현금(+)
    private BigDecimal amount;

    private String description;

    @Enumerated(EnumType.STRING)
    private DepositTransactionType type;

    @Column(name="occurred_at")
    private LocalDateTime occurredAt;

    // 연관관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="account_id")
    private Account account;

    public static DepositTransaction create(BigDecimal amount, String description, DepositTransactionType type, Account account) {
        DepositTransaction dt = new DepositTransaction();
        dt.amount = amount;
        dt.description = description;
        dt.type = type;
        dt.occurredAt = LocalDateTime.now();
        dt.account = account;
        return dt;
    }

}
