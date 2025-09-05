package com.fintory.domain.stock.dto.token;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DBTokenResponse(
       @JsonProperty("access_token") String token,
       @JsonProperty("expire_in") int expiredIn
) {
}
