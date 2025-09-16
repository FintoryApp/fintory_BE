package com.fintory.domain.portfolio.model;

import com.fintory.domain.account.model.Account;
import com.fintory.domain.common.BaseEntity;
import com.fintory.domain.stock.model.Stock;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Getter
@Builder
@AllArgsConstructor
@Table(name="owned_stock")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OwnedStock extends BaseEntity {

    private Integer quantity;

    @Column(name="average_purchase_price")
    private BigDecimal averagePurchasePrice; // 한 주당 평균 매입 가격

    @Column(name="purchase_amount")
    private BigDecimal purchaseAmount; //총 매수 금액

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="stock_id")
    private Stock stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="account_id")
    private Account account;

    public void updateOwnedStockPurchase(Integer quantity, BigDecimal purchaseAmount){
        this.quantity = this.quantity + quantity;
        this.purchaseAmount = this.purchaseAmount.add(purchaseAmount);
        this.averagePurchasePrice = this.purchaseAmount
                .divide(BigDecimal.valueOf(this.quantity), 4, RoundingMode.HALF_UP);
    }

    public void updateOwnedStockSell(Integer quantity,BigDecimal soldPurchaseAmount){
        this.quantity = this.quantity - quantity;
        this.purchaseAmount = this.purchaseAmount.subtract(soldPurchaseAmount);

        if (this.quantity > 0) {
            this.averagePurchasePrice = this.purchaseAmount
                    .divide(BigDecimal.valueOf(this.quantity), 4, RoundingMode.HALF_UP);
        } else {
            // 모든 주식을 판매한 경우
            this.averagePurchasePrice = BigDecimal.ZERO;
        }
    }
}
