package com.fintory.domain.stock.model;


import com.fintory.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Builder
@AllArgsConstructor
@Table(name="live_stock_price")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LiveStockPrice extends BaseEntity {

    @Column(name="current_price")
    private int currentPrice;

    @Column(name="price_change")
    private BigDecimal priceChange;

    @Column(name="price_change_rate")
    private BigDecimal priceChangeRate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="stock_id")
    private Stock stock;

    private String time;

    public LiveStockPrice update(BigDecimal priceChange, BigDecimal priceChangeRate, int currentPrice) {
        this.priceChange = priceChange;
        this.priceChangeRate = priceChangeRate;
        this.currentPrice = currentPrice;

        return this;
    }



}
