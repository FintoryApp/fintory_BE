package com.fintory.child.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class KisWebSocketTokenRequest {

    @JsonProperty("grant_type")
    @Builder.Default
    private String grantType="client_credentials";

    @JsonProperty("appkey")
    private String appkey;

    @JsonProperty("secretkey")
    private String secretkey;

}
