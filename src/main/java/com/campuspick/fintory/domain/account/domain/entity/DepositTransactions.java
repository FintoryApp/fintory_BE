package com.campuspick.fintory.domain.account.domain.entity;

import com.campuspick.fintory.global.entity.BaseTimeEntity;
import com.campuspick.fintory.domain.parent.domain.entity.Parents;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name="deposit_transactions")
public class DepositTransactions extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

    private int amount;

    @Column(name="executed_at")
    private LocalDateTime executedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="account_id")
    private Accounts account;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="sender_parent_id")
    private Parents parent;
}
