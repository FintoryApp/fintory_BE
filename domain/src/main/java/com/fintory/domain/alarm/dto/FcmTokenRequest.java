package com.fintory.domain.alarm.dto;


import jakarta.validation.constraints.NotBlank;

public record FcmTokenRequest(
        @NotBlank(message="토큰을 입력해야 합니다.")
        String token
) {
}
