package com.campuspick.fintory.domain.account.domain.entity;

import com.campuspick.fintory.domain.child.domain.entity.Child;
import com.campuspick.fintory.domain.consulting.domain.entity.Report;
import com.campuspick.fintory.domain.portfolio.domain.entity.OwnedStock;
import com.campuspick.fintory.domain.portfolio.domain.entity.StockTransaction;
import com.campuspick.fintory.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Table(name="accounts")
public class Account extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @OneToMany(cascade=CascadeType.ALL,mappedBy="account")
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
