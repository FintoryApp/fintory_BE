package com.fintory.domain.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EODResponseWrapper(
        @JsonProperty("data") List<EODResponse> eodResponseList
) {
}
