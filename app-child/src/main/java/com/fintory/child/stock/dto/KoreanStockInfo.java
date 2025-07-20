package com.fintory.child.stock.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintory.domain.stock.model.Stock;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KoreanStockInfo {

    @JsonAlias("pdno")
    private String code;

    @JsonAlias("excg_dvsn_cd")
    private String marketName;

    @JsonAlias("prdt_abrv_name") // 상품명
    private String name;

    @JsonAlias("prdt_eng_abrv_name") //상품 영문명
    private String engName;

    @JsonAlias("std_idst_clsf_cd_name") //지수업종대분류코드명
    private String category;

    public static Stock toStock(KoreanStockInfo stockInfo){
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
