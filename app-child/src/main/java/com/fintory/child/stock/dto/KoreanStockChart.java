package com.fintory.child.stock.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.model.StockPriceHistory;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

@Builder
@Getter
public class KoreanStockChart {

    @JsonAlias("stck_bsop_date") //수신 별명 (송신 전용은 JsonGetter)
    private String time;

    @JsonAlias("stck_oprc")
    private BigDecimal openPrice;

    @JsonAlias("stck_hgpr")
    private BigDecimal highPrice;

    @JsonAlias("stck_lwpr")
    private BigDecimal  lowPrice;

    @JsonAlias("stck_clpr")
    private BigDecimal closePrice;

    @JsonAlias("acml_vol")
    private BigDecimal volume;

    public static StockPriceHistory toStockPriceHistory(KoreanStockChart koreanStockChart, Stock stock) {
        LocalDate formattedDate = LocalDate.parse(koreanStockChart.getTime().replaceAll("(\\d{4})(\\d{2})(\\d{2})", "$1-$2-$3"));
        return StockPriceHistory.builder()
                .openPrice(koreanStockChart.getOpenPrice())
                .closePrice(koreanStockChart.getClosePrice())
                .highPrice(koreanStockChart.getHighPrice())
                .lowPrice(koreanStockChart.getLowPrice())
                .volume(koreanStockChart.getVolume())
                .time(formattedDate)
                .stock(stock)
                .build();
    }

}
