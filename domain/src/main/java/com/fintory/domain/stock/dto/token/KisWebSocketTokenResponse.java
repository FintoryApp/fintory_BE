package com.fintory.domain.stock.dto.token;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KisWebSocketTokenResponse (
    @JsonProperty("approval_key") String approvalKey
) { }
