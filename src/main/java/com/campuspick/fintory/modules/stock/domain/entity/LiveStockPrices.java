package com.campuspick.fintory.modules.stock.domain.entity;


import com.campuspick.fintory.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name="live_stock_prices")
public class LiveStockPrices extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name="stock_id")
    private Stocks stock;

    @Column(name="current_price")
    private int currentPrice;

    @Column(name="price_change")
    private int priceChange;

    @Column(name="price_change_rate")
    private int priceChangeRate;
}
