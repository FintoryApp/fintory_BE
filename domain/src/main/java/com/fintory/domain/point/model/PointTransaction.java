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

    private PointTransaction(int amount, PointTransactionType type, PointTransactionSource source, PointWallet pointWallet) {
        this.amount = amount;
        this.type = type;
        this.source = source;
        this.pointWallet = pointWallet;
    }

    public static PointTransaction earn(int amount, PointTransactionSource source, PointWallet pointWallet) {
        return new PointTransaction(amount, PointTransactionType.EARN, source, pointWallet);
    }

    public static PointTransaction use(int amount, PointTransactionSource source, PointWallet pointWallet) {
        return new PointTransaction(amount, PointTransactionType.WITHDRAW, source, pointWallet);
    }

}
