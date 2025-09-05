package com.fintory.domain.stock.dto.token;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KisTokenResponse(

        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("expires_in")
        Integer expiresIn,

        @JsonProperty("access_token_token_expired")
        String accessTokenExpired
) {
}
