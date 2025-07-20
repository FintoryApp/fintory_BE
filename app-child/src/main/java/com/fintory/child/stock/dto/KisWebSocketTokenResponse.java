package com.fintory.child.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KisWebSocketTokenResponse {

    @JsonProperty("approval_key")
    String approvalKey;
}
