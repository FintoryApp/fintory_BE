package com.fintory.domain.stock.dto.token;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KisWebSocketTokenRequest(
        String appkey,
        String secretkey,
        @JsonProperty("grant_type")  String grantType
){}
