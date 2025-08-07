package com.fintory.auth.dto.response;

public record SignUpResponse(

        String accessToken,
        String refreshToken,
        String nickname,
        String email

) {
}
