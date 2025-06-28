package com.fintory.domain.stock.model;

import com.fintory.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name="stock_price_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockPriceHistory extends BaseEntity {

    @Column(name="open_price")
    private int openPrice;

    @Column(name="close_price")
    private int closePrice;

    @Column(name="high_price")
    private int highPrice;

    @Column(name="low_price")
    private int lowPrice;

    private int volume; //거래량

    @Enumerated(EnumType.STRING)
    private IntervalType intervalType;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="stock_id")
    private Stock stock;

}

