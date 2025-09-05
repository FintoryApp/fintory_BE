package com.fintory.domain.stock.dto.token;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KisTokenRequest (
       String appkey,
       String appsecret,
       @JsonProperty("grant_type") String grantType
){
}
