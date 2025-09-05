package com.fintory.domain.stock.dto.overseas.core;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;


import java.math.BigDecimal;


public record OverseasStockPriceHistory (

    @JsonAlias("ovrs_nmix_oprc")
     BigDecimal openPrice,

    @JsonAlias("ovrs_nmix_hgpr")
     BigDecimal highPrice,

    @JsonAlias("ovrs_nmix_lwpr")
     BigDecimal  lowPrice,

    @JsonAlias("ovrs_nmix_prpr")
     BigDecimal closePrice,

    @JsonAlias("stck_bsop_date")
    String time

    ){}
