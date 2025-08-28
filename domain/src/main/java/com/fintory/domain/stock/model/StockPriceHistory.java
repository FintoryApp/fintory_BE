package com.fintory.domain.stock.model;

import com.fintory.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@Table(name="stock_price_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockPriceHistory extends BaseEntity {

    @Column(name="open_price")
    private BigDecimal openPrice;

    @Column(name="close_price")
    private BigDecimal closePrice;

    @Column(name="high_price")
    private BigDecimal highPrice;

    @Column(name="low_price")
    private BigDecimal lowPrice;

    @Enumerated(EnumType.STRING)
    private IntervalType intervalType;

    @Column(name = "date")
    private LocalDate date;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="stock_id")
    private Stock stock;

    public StockPriceHistory updateStockPriceHistory(BigDecimal openPrice, BigDecimal closePrice, BigDecimal highPrice, BigDecimal lowPrice) {
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;

        return this;
    }

}

