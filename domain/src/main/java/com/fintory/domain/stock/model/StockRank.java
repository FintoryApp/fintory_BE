package com.fintory.domain.stock.model;

import com.fintory.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Builder
@AllArgsConstructor
@Getter
@Table(name="stock_rank")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockRank extends BaseEntity {

    @Column(name="roc_rank")
    private int rocRank;

    @Column(name="trading_volume_rank")
    private int tradingVolumeRank;

    @Column(name="market_cap_rank")
    private int marketCapRank;

    @Column(name="market_cap")
    private BigDecimal marketCap;

    @Column(name="rise_rate")
    private BigDecimal riseRate;

    @Column(name="trading_volume")
    private BigDecimal tradingVolume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="stock_id")
    private Stock stock;
}
