package com.campuspick.fintory.domain.portfolio.domain.entity;


import com.campuspick.fintory.domain.account.domain.entity.Accounts;
import com.campuspick.fintory.domain.stock.domain.entity.Stocks;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransactions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int amount;

    @Column(name = "price_per_share", precision = 15, scale = 2)
    private float pricePerShare;

    private int quantity;

    @Column(name = "request_date")
    private LocalDateTime requestDate;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "exchange_rate", precision = 10, scale = 4)
    private BigDecimal exchangeRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "market_type")
    private MarketType marketType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stocks stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="account_id")
    private Accounts account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="owned_stock_id")
    private OwnedStocks ownedStock;

}