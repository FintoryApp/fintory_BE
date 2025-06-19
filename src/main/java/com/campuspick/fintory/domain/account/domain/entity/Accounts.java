package com.campuspick.fintory.domain.account.domain.entity;

import com.campuspick.fintory.domain.child.domain.entity.Childs;
import com.campuspick.fintory.domain.portfolio.domain.entity.OwnedStocks;
import com.campuspick.fintory.domain.portfolio.domain.entity.StockTransactions;
import com.campuspick.fintory.global.entity.BaseTimeEntity;
import com.campuspick.fintory.domain.consulting.domain.entity.Reports;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Table(name="accounts")
public class Accounts extends BaseTimeEntity {

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
    private List<DepositTransactions> depositTransactions;

    @OneToMany(cascade=CascadeType.ALL,mappedBy="account")
    private List<OwnedStocks> ownedStocks;

    @OneToMany(cascade=CascadeType.ALL,mappedBy="account")
    private List<StockTransactions> stockTransactions;

    @OneToMany(cascade=CascadeType.ALL,mappedBy="account")
    private List<Reports> reports;

    @OneToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="child_id")
    private Childs child;
}
