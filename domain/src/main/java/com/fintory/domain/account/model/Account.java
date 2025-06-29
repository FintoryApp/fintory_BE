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
import java.util.List;

@Entity
@Getter
@Table(name="account")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseEntity {

    @Column(name="account_number")
    private String accountNumber;

    @Column(precision=15,  scale=2)
    private BigDecimal balance;

    private boolean status;

    @Column(name="available_cash")
    private int availableCash;

    @Column(name="total_assets")
    private int totalAssets;

    @Column(name="total_purchase")
    private int totalPurchase;

    @Column(name="total_valuation")
    private int totalValuation;

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
}


