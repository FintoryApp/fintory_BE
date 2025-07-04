package com.fintory.child.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockInfoHeader {

    @JsonProperty("content-type")
    private String contentType="application/json;charset=utf-8";

    @JsonProperty("authorization")
    private String authorization;

    @JsonProperty("appkey")
    private String appkey;

    @JsonProperty("appsecret")
    private String appsecret;

    @JsonProperty("tr_id")
    private String trId="CTPF1002R";

    @JsonProperty("custtype")
    private String custtype="P";
}
