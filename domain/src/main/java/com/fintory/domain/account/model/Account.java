package com.fintory.domain.account.model;

import com.fintory.domain.child.model.Child;
import com.fintory.domain.common.BaseEntity;
import com.fintory.domain.consulting.model.Report;
import com.fintory.domain.portfolio.model.OwnedStock;
import com.fintory.domain.portfolio.model.StockTransaction;
import com.fintory.domain.portfolio.model.TransactionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Table(name="account")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseEntity {

    @Column(name="account_number")
    private String accountNumber;

    @Column(precision=15,  scale=2)
    private BigDecimal balance; //REVIEW 저희 미체결 주문이 없으니 balance와 avaiableCash를 동일시하는 것이 어떤지

    private boolean status;

    @Column(name="available_cash")
    private BigDecimal availableCash;

    @Column(name="total_assets")
    private BigDecimal totalAssets;

    @Column(name="total_purchase")
    private BigDecimal totalPurchase;

    @Column(name="total_valuation")
    private BigDecimal totalValuation;

    @OneToMany(cascade= CascadeType.ALL,mappedBy="account")
    private List<DepositTransaction> depositTransactions;

    @OneToMany(cascade=CascadeType.ALL,mappedBy="account")
    private List<OwnedStock> ownedStocks;

    @OneToMany(cascade=CascadeType.ALL,mappedBy="account")
    private List<StockTransaction> stockTransactions;

    @OneToMany(cascade=CascadeType.ALL,mappedBy="account")
    private List<Report> reports;

    @OneToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="child_id")
    private Child child;


    public void updateSellStock(BigDecimal sellPrice, BigDecimal sellPurchaseAmount){
        this.availableCash = this.availableCash.add(sellPrice);
        this.totalPurchase = this.totalPurchase.subtract(sellPurchaseAmount);

        updateTotalValuation();
        updateTotalAssets();
    }

    public void updatePurchaseStock(BigDecimal purchasePrice){
        this.availableCash = this.availableCash.subtract(purchasePrice);
        this.totalPurchase = this.totalPurchase.add(purchasePrice);

        updateTotalValuation();
        updateTotalAssets();
    }

    //NOTE DB에는 현재 부정확한 평가금액이 저장됨. (현재가를 실시간으로 받아오지만 평가금액을 update하는 로직이 없음)
    // 그러나 포트폴리오를 조회할 땐 평가금액 대신 계산에 필요한 정확한 데이터(quantity, avgPurchasePrice, totalInvestment 등)를 제공하기 때문에 현재 valuationAmount, returnRate 등은 거래 시점에서의 기록용으로만 사용하고 있음
    // => 이는 프론트에서 값을 계산해서 보여주고 있기 때문

    // REVIEW DB에는 거래 기록과 보유 수량만 저장하고 현재 평가금액은 조회 시점에서 계산하는 현재 방식이 -> DB를 계속 업데이트(실시간)하는 것보다 효율적이라고 생각함.
    // 단 필드명을 바꾸는 것 정도는 고려해볼만 함
    public void updateTotalValuation(){
        BigDecimal totalValuation = BigDecimal.ZERO;
        for(OwnedStock ownedStock : ownedStocks){
            totalValuation = totalValuation.add(ownedStock.getValuationAmount());
        }

        this.totalValuation = totalValuation;
    }

    public void updateTotalAssets(){
        BigDecimal totalAssets = this.totalValuation.add(this.availableCash);
        this.totalAssets = totalAssets;
    }
}


