package com.fintory.child.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class KisTokenResponse {

    @JsonProperty("access_token")
    String accessToken;

    @JsonProperty("token_type")
    String tokenType;

    @JsonProperty("expires_in")
    String expiresIn;

    @JsonProperty("access_token_token_expired")
    String accessTokenExpired;
}
