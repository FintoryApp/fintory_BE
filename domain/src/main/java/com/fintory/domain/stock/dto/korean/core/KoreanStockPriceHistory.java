package com.fintory.domain.stock.dto.korean.core;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KoreanStockPriceHistory (

    @JsonAlias("stck_oprc")
     BigDecimal openPrice,

    @JsonAlias("stck_hgpr")
     BigDecimal highPrice,

    @JsonAlias("stck_lwpr")
     BigDecimal  lowPrice,

    @JsonAlias("stck_clpr")
     BigDecimal closePrice,

    @JsonAlias("stck_bsop_date")
    String time

){}
