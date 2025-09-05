package com.fintory.domain.portfolio.model;

import com.fintory.domain.account.model.Account;
import com.fintory.domain.common.BaseEntity;
import com.fintory.domain.stock.model.Stock;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

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

    @Column(name="valuation_profit_and_loss")
    private BigDecimal valuationProfitAndLoss; //현재 평가 손익(평가금액 - 매수금액)

    @Column(name="return_rate")
    private BigDecimal returnRate; //수익률

    @Column(name="valuation_amount")
    private BigDecimal valuationAmount; //현재 평가 금액(quantity * 현재 주가)

    @Column(name="purchase_amount")
    private BigDecimal purchaseAmount; //총 매수 금액

    @OneToMany(cascade = CascadeType.ALL, mappedBy ="ownedStock")
    private List<StockTransaction> stockTransactions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="stock_id")
    private Stock stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="account_id")
    private Account account;

    public void updateOwnedStockPurchase(Integer quantity, BigDecimal purchaseAmount, BigDecimal livePrice){
        this.quantity = this.quantity + quantity;
        this.purchaseAmount = this.purchaseAmount.add(purchaseAmount);
        this.valuationAmount = livePrice.multiply(BigDecimal.valueOf(this.quantity));
        this.averagePurchasePrice = this.purchaseAmount
                .divide(BigDecimal.valueOf(this.quantity), 4, RoundingMode.HALF_UP);
        this.valuationProfitAndLoss = this.valuationAmount.subtract(this.purchaseAmount); // 평가손익 = 평가금액 - 매수금액

        //0으로 나누기 방지
        if (this.purchaseAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.returnRate = this.valuationProfitAndLoss
                    .divide(this.purchaseAmount, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        } else {
            this.returnRate = BigDecimal.ZERO;
        }
    }

    public void updateOwnedStockSell(Integer quantity, BigDecimal livePrice,BigDecimal soldPurchaseAmount){
        this.quantity = this.quantity - quantity;
        this.purchaseAmount = this.purchaseAmount.subtract(soldPurchaseAmount);

        if (this.quantity > 0) {
            this.valuationAmount = livePrice.multiply(BigDecimal.valueOf(this.quantity));
            this.averagePurchasePrice = this.purchaseAmount
                    .divide(BigDecimal.valueOf(this.quantity), 4, RoundingMode.HALF_UP);
            this.valuationProfitAndLoss = this.valuationAmount.subtract(this.purchaseAmount);

            //0으로 나누기 방지
            if (this.purchaseAmount.compareTo(BigDecimal.ZERO) > 0) {
                this.returnRate = this.valuationProfitAndLoss
                        .divide(this.purchaseAmount, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100")); //수익률 = (평가손익 / 매입원가) × 100
            } else {
                this.returnRate = BigDecimal.ZERO;
            }
        } else {
            // 모든 주식을 판매한 경우
            this.valuationAmount = BigDecimal.ZERO;
            this.averagePurchasePrice = BigDecimal.ZERO;
            this.valuationProfitAndLoss = BigDecimal.ZERO;
            this.returnRate = BigDecimal.ZERO;
        }
    }
}
