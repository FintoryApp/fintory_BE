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

    @Column(name="roc_rate")
    private BigDecimal rocRate;

    @Column(name="trading_volume")
    private Long tradingVolume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="stock_id")
    private Stock stock;

    public StockRank updateStockRankData(BigDecimal marketCap, BigDecimal rocRate, Long tradingVolume) {
        this.marketCap = marketCap;
        this.rocRate = rocRate;
        this.tradingVolume = tradingVolume;
        return this;
    }

    public StockRank updateMarketCapRank(int marketCapRank) {
        this.marketCapRank=marketCapRank;
        return this;
    }

    public StockRank updateRocRank(int rocRank) {
        this.rocRank=rocRank;
        return this;
    }
    public StockRank updateTradingVolumeRank(int tradingVolumeRank) {
        this.tradingVolumeRank=tradingVolumeRank;
        return this;
    }

}
