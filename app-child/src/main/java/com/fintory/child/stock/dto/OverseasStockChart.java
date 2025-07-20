package com.fintory.child.stock.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverseasStockChart {


    @JsonAlias("stck_bsop_date")
    private String time;

    @JsonAlias("ovrs_nmix_oprc")
    private float openPrice;

    @JsonAlias("ovrs_nmix_hgpr")
    private float highPrice;

    @JsonAlias("ovrs_nmix_lwpr")
    private float  lowPrice;

    @JsonAlias("ovrs_nmix_prpr")
    private float closePrice;

    @JsonProperty("acml_vol")
    private long volume;


}
