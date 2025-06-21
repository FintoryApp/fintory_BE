package com.campuspick.fintory.domain.stock.domain.entity;


import com.campuspick.fintory.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name="live_stock_prices")
public class LiveStockPrice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

    @Column(name="current_price")
    private int currentPrice;

    @Column(name="price_change")
    private int priceChange;

    @Column(name="price_change_rate")
    private int priceChangeRate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="stock_id")
    private Stock stock;
}
