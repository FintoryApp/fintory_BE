package com.campuspick.fintory.domain.stock.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name="stock_price_histories")
public class StockPriceHistories {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

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
    private Stocks stock;

}

