package com.fintory.domain.account.model;

import com.fintory.domain.child.model.Child;
import com.fintory.domain.common.BaseEntity;
import com.fintory.domain.consulting.model.Report;
import com.fintory.domain.portfolio.model.OwnedStock;
import com.fintory.domain.portfolio.model.StockTransaction;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name="account")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseEntity {

    private boolean status;

    @Column(name="available_cash")
    private BigDecimal availableCash;

    @Column(name="total_assets")
    private BigDecimal totalAssets;

    @Column(name="total_purchase")
    private BigDecimal totalPurchase;

    @Column(name="total_valuation")
    private BigDecimal totalValuation;

    // 1:1
    @OneToOne
    @JoinColumn(name="child_id")
    private Child child;

    // 1:N
    @OneToMany(cascade= CascadeType.ALL, mappedBy="account")
    private List<DepositTransaction> depositTransactions = new ArrayList<>();

    @OneToMany(cascade=CascadeType.ALL, mappedBy="account")
    private List<StockTransaction> stockTransactions = new ArrayList<>();

    @OneToMany(cascade=CascadeType.ALL, mappedBy="account")
    private List<OwnedStock> ownedStocks = new ArrayList<>();

    @OneToMany(cascade=CascadeType.ALL,mappedBy="account")
    private List<Report> reports;

    // 생성 + 도메인 -> 생성자 대신 정적 팩토리 메소드
    public static Account createWithInitialDeposit(Child child, BigDecimal initialAmount) {
        Account account = new Account();
        account.child = child;
        account.status = true;
        account.availableCash = initialAmount;
        account.totalPurchase = BigDecimal.ZERO;
        account.totalValuation = BigDecimal.ZERO;
        account.totalAssets = initialAmount;

        DepositTransaction deposit = DepositTransaction.create(initialAmount, "기본금 지급", DepositTransactionType.DEPOSIT);
        account.addDepositTransaction(deposit);

        return account;
    }

    // 연관관계 편의 메소드
    public void addDepositTransaction(DepositTransaction tx) {
        this.depositTransactions.add(tx);
        tx.setAccount(this);
    }


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

    private void updateTotalValuation(){
        BigDecimal totalValuation = BigDecimal.ZERO;
        for(OwnedStock ownedStock : ownedStocks){
            totalValuation = totalValuation.add(ownedStock.getValuationAmount());
        }

        this.totalValuation = totalValuation;
    }

    private void updateTotalAssets(){
        BigDecimal totalAssets = this.totalValuation.add(this.availableCash);
        this.totalAssets = totalAssets;
    }
}


