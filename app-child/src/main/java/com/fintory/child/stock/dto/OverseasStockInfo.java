package com.fintory.child.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintory.domain.stock.model.Stock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverseasStockInfo {

    @JsonProperty("std_pdno")
    private String code;

    @JsonProperty("tr_mket_name")
    private String marketName;

    @JsonProperty("prdt_name")
    private String name;

    @JsonProperty("prdt_eng_name")
    private String engName;

    @JsonProperty("prdt_clsf_name")
    private String category;

    public static Stock toStock(OverseasStockInfo stockInfo){
        return Stock.builder()
                .code(stockInfo.code)
                .marketName(stockInfo.marketName)
                .currencyName("KRW")
                .name(stockInfo.name)
                .engName(stockInfo.engName)
                .category(stockInfo.category)
                .build();
    }
}
