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


