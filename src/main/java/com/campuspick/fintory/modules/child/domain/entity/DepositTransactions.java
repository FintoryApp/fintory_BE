package com.campuspick.fintory.modules.child.domain.entity;

import com.campuspick.fintory.global.entity.BaseTimeEntity;
import com.campuspick.fintory.modules.parent.domain.entity.Parents;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name="deposit_transactions")
public class DepositTransactions extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="account_id")
    private Account account;

    private int amount;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="sender_parent_id")
    private Parents parent;
}
