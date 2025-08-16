package com.fintory.domain.stock.model;

import com.fintory.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name="stock_price_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
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
    @Column(name="interval_type",length = 15)
    private IntervalType intervalType;

    private String date;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="stock_id")
    private Stock stock;

}

