package com.fintory.domain.stock.model;

import com.fintory.domain.common.BaseEntity;
import com.fintory.domain.stock.dto.EODResponse;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

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
    private double tradingVolume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="stock_id")
    private Stock stock;


    public StockRank updateVolumeStockRank(int tradingVolumeRank) {
        this.tradingVolumeRank=tradingVolumeRank;
        return this;
    }

    public StockRank updateROCStockRank(int rocRank) {
        this.rocRank=rocRank;
        return this;
    }

    public StockRank updateVolumeAndROC(List<EODResponse> response) {
            // 최신 데이터 (인덱스 0)와 이전 데이터 (인덱스 1)
            EODResponse today = response.get(0);
            EODResponse yesterday = response.get(1);

            // 거래량
            double volume = today.volume();

            // 등락률 계산: (오늘 종가 - 어제 종가) / 어제 종가 * 100
            BigDecimal todayClose = today.close();
            BigDecimal yesterdayClose = yesterday.close();

            BigDecimal rocRate = null;
            if (todayClose != null && yesterdayClose != null &&
                    yesterdayClose.compareTo(BigDecimal.ZERO) != 0) {

                rocRate= todayClose.subtract(yesterdayClose)
                        .divide(yesterdayClose, 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            this.rocRate = rocRate;
            this.tradingVolume = volume;

        return this;
    }


}
