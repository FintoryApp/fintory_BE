package com.fintory.auth.dto;

import lombok.Builder;

@Builder
public record AuthToken(
        String accessToken,
        String refreshToken
) {

}
