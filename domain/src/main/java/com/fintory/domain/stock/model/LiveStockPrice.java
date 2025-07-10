package com.fintory.domain.stock.model;


import com.fintory.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name="live_stock_price")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LiveStockPrice extends BaseEntity {

    @Column(name="current_price")
    private int currentPrice;

    @Column(name="price_change")
    private String priceChange;

    @Column(name="price_change_rate")
    private String priceChangeRate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="stock_id")
    private Stock stock;


    public LiveStockPrice update(String priceChange, String priceChangeRate, int currentPrice) {
        this.priceChange = priceChange;
        this.priceChangeRate = priceChangeRate;
        this.currentPrice = currentPrice;

        return this;
    }



}
