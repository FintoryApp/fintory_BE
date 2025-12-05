package com.fintory.websocketwebflux.dto;

public record Tick(
        String code,
        int price,
        int volume,
        String timestamp
) {}