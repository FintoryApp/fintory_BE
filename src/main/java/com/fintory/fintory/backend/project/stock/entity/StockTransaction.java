package com.fintory.fintory.backend.project.stock.entity;





import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransaction {

    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_id")
    private Long stockId;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "owned_stock_id")
    private Long ownedStockId;

    /**
     * 거래 총 대금
     */
    private Integer amount;

    /**
     * 주식을 살 때 현재 주당 가격
     */
    @Column(name = "price_per_share", precision = 15, scale = 2)
    private BigDecimal pricePerShare;

    private Integer quantity;

    @Column(name = "request_date")
    private LocalDateTime requestDate;

    /**
     * 실제 체결 시 날짜
     */
    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "exchange_rate", precision = 10, scale = 4)
    private BigDecimal exchangeRate;

    /**
     * 매수 / 매도 구분
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType;

    /**
     * 거래 상태- pending, completed, canceled, failed
     */
    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "market_type")
    private MarketType marketType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // ENUM 클래스들
    public enum TransactionType {
        BUY, SELL
    }

    public enum Status {
        PENDING, COMPLETED, CANCELED, FAILED
    }

    public enum MarketType {
        DOMESTIC, OVERSEAS
    }
}
