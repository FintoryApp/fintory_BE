package com.fintory.domain.portfolio.model;

import com.fintory.domain.account.model.Account;
import com.fintory.domain.common.BaseEntity;
import com.fintory.domain.stock.model.Stock;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Table(name="owned_stock")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OwnedStock extends BaseEntity {

    @Column(precision=15, scale=3)
    private BigDecimal quantity;

    @Column(name="average_purchase_price", precision=12, scale=2)
    private BigDecimal averagePurchasePrice; // 한 주당 평균 매입 가격

    @Column(name="valuation_profit_and_loss", precision=3, scale=2)
    private BigDecimal valuationProfitAndLoss; //현재 평가 손익(평가금액 - 매수금액)

    @Column(name="return_rate",precision=3, scale=2)
    private BigDecimal returnRate; //수익률

    @Column(name="valuation_amount", precision=12,scale=2)
    private BigDecimal valuationAmount; //현재 평가 금액(quantity * 현재 주가)

    @Column(name="purchase_amount",precision=12, scale=2)
    private BigDecimal purchaseAmount; //총 매수 금액

    @OneToMany(cascade = CascadeType.ALL, mappedBy ="ownedStock")
    private List<StockTransaction> stockTransactions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="stock_id")
    private Stock stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="account_id")
    private Account account;
}
