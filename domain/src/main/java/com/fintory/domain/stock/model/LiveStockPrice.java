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
    private BigDecimal currentPrice;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="stock_id")
    private Stock stock;

    public LiveStockPrice updateLiveStockPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
        return this;

    }
}
