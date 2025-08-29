package com.fintory.domain.account.model;


import com.fintory.domain.common.BaseEntity;
import com.fintory.domain.parent.model.Parent;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@Table(name="deposit_transaction")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DepositTransaction extends BaseEntity {

    private BigDecimal amount;

    @Column(name="executed_at")
    private LocalDateTime executedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="account_id")
    private Account account;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="sender_parent_id")
    private Parent parent;
}
