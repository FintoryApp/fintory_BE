package com.fintory.domain.stock.model;


import com.fintory.domain.common.BaseEntity;
import com.fintory.domain.stock.dto.EODResponse;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Table(name="live_stock_price")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class LiveStockPrice extends BaseEntity {

    @Column(name="current_price")
    private BigDecimal currentPrice;

    @Column(name="price_change")
    private BigDecimal priceChange;

    @Column(name="price_change_rate")
    private BigDecimal priceChangeRate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="stock_id")
    private Stock stock;

    public LiveStockPrice updateLiveStockPrice(List<EODResponse> response){
            // 최신 데이터 (인덱스 0)와 전일 데이터 (인덱스 1)
            BigDecimal todayClose = response.get(0).close();
            BigDecimal yesterdayClose = response.get(1).close();

            BigDecimal rate=null;

            // 등락률 계산: (오늘 종가 - 어제 종가) / 어제 종가 * 100
            if(todayClose!=null &&  yesterdayClose!=null &&
                yesterdayClose.compareTo(BigDecimal.ZERO)!=0 && todayClose.compareTo(BigDecimal.ZERO)!=0){
                rate = (todayClose.subtract(yesterdayClose)
                        .divide(yesterdayClose, 2, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100)));
            }

            this.currentPrice = todayClose;
            this.priceChange = todayClose.subtract(yesterdayClose);
            this.priceChangeRate = rate;
            return this;
    }

    public LiveStockPrice updateLiveStockPrice(BigDecimal currentPrice,LiveStockPrice liveStockPrice){
        if(liveStockPrice.priceChange!=null){
            this.currentPrice = currentPrice;
            this.priceChange = currentPrice.subtract(liveStockPrice.priceChange);
            this.priceChangeRate = priceChange.divide(liveStockPrice.priceChange, 2, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            return this;
        }
        return liveStockPrice;
    }
}
