package com.fintory.domain.point.model;

import com.fintory.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Table(name="point_transaction")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointTransaction extends BaseEntity {

    private int amount;

    @Enumerated(EnumType.STRING)
    private PointTransactionType type;

    @Enumerated(EnumType.STRING)
    private PointTransactionSource source;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_id")
    private PointWallet pointWallet;

    public static PointTransaction createEarningTransaction(int amount, PointTransactionSource source, PointWallet pointWallet) {
        PointTransaction pt = new PointTransaction();
        pt.amount = amount;
        pt.type = PointTransactionType.EARN;
        pt.source = source;
        pt.pointWallet = pointWallet;
        return pt;
    }

    public static PointTransaction createWithdrawTransaction(int amount, PointTransactionSource source, PointWallet pointWallet) {
        PointTransaction pt = new PointTransaction();
        pt.amount = amount;
        pt.type = PointTransactionType.WITHDRAW;
        pt.source = source;
        pt.pointWallet = pointWallet;
        return pt;
    }
}
