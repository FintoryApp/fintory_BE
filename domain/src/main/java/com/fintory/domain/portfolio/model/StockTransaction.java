package com.fintory.domain.portfolio.model;

import com.fintory.domain.account.model.Account;
import com.fintory.domain.common.BaseEntity;
import com.fintory.domain.stock.model.Stock;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@Table(name = "stock_transaction")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    @Column(name = "price_per_share", precision = 15, scale = 2)
    private BigDecimal pricePerShare;

    private int quantity;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "exchange_rate", precision = 10, scale = 4)
    private BigDecimal exchangeRate; //NOTE 평가 금액 업데이트 시 현재 환율을 다시 적용해야함. 여긴 매수시 환율

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "market_type")
    private MarketType marketType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="owned_stock_id")
    private OwnedStock ownedStock;

}