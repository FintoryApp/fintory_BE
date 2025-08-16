package com.fintory.domain.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record IntraDayResponseWrapper(
        @JsonProperty("data") List<IntraDayResponse> intraDayResponseList
) { }
