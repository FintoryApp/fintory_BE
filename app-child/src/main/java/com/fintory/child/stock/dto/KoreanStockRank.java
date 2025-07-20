package com.fintory.child.stock.dto;

import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.model.StockRank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KoreanStockRank {

    private int rocRank;
    private int tradingVolumeRank;
    private int marketCapRank;
    private String code;
    private BigDecimal marketCap;
    private BigDecimal riseRate;
    private BigDecimal tradingVolume;


    public static StockRank toStockRank(KoreanStockRank koreanStockRank, Stock stock) {
        return StockRank.builder()
                .rocRank(koreanStockRank.getRocRank())
                .tradingVolumeRank(koreanStockRank.getTradingVolumeRank())
                .marketCapRank(koreanStockRank.getMarketCapRank())
                .stock(stock)
                .build();
    }
}
