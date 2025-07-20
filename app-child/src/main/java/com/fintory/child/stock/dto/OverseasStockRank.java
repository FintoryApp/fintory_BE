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
public class OverseasStockRank {

    private int rocRank;
    private int tradingVolumeRank;
    private int marketCapRank;
    private String code;
    private BigDecimal marketCap;
    private BigDecimal riseRate;
    private BigDecimal tradingVolume;


    public static StockRank toStockRank(OverseasStockRank overseasStockRank, Stock stock) {
        return StockRank.builder()
                .rocRank(overseasStockRank.getRocRank())
                .tradingVolumeRank(overseasStockRank.getTradingVolumeRank())
                .marketCapRank(overseasStockRank.getMarketCapRank())
                .stock(stock)
                .build();
    }

}
