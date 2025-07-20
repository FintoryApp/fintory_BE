package com.fintory.child.stock.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintory.domain.stock.model.LiveStockPrice;
import com.fintory.domain.stock.model.Stock;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KoreanStockRealPrice implements Serializable {

    @JsonAlias("MKSC_SHRN_ISCD")
    private String code;

    @JsonAlias("STCK_CNTG_HOUR")
    private String time;

    @JsonAlias("STCK_PRPR")
    private int currentPrice;

    @JsonAlias("PRDY_VRSS ") //전일대비
    private BigDecimal priceChange;

    @JsonAlias("PRDY_CTRT")
    private BigDecimal priceChangeRate;

    public static LiveStockPrice toLiveStockPrice(KoreanStockRealPrice koreanStockRealPrice, Stock stock){
        return LiveStockPrice.builder()
                .currentPrice(koreanStockRealPrice.currentPrice)
                .priceChange(koreanStockRealPrice.getPriceChange())
                .priceChangeRate(koreanStockRealPrice.getPriceChangeRate())
                .time(koreanStockRealPrice.getTime())
                .stock(stock)
                .build();

    }

}
