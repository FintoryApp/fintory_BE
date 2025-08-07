package com.fintory.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReissueRequest(
        @NotBlank(message = "리프레시 토큰이 필요합니다.")
        String refreshToken
) {
}
