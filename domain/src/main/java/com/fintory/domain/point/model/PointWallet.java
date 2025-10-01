package com.fintory.domain.point.model;

import com.fintory.domain.child.model.Child;
import com.fintory.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name="point")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointWallet extends BaseEntity {
    // 이렇게 되면 포인트 테이블이 아닌 포인트 history 테이블이 됨. -> amount를 계산하려면 SUM(amount)가 된다는 사실 기억

    private int totalAmount;

    @OneToOne
    @JoinColumn(name="child_id")
    private Child child;

    @OneToMany(mappedBy = "pointWallet", cascade = CascadeType.ALL)
    private List<PointTransaction> transactions = new ArrayList<>();

    public static PointWallet createWithInitialPointWallet(Child child) {

        PointWallet pointWallet = new PointWallet();
        pointWallet.child = child;
        pointWallet.totalAmount = 0;

        return pointWallet;
    }

    public void earnPoint(int amount) {
        this.totalAmount += amount;
    }

    public void exchangePoint(int point) {
    }
    public void withdrawPoint(int amount) {
        this.totalAmount -= amount;
    }

    public void addTransaction(PointTransaction pt) {
        this.transactions.add(pt);
        pt.setPointWallet(this);
    }

}
