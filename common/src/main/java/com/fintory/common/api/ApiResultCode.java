package com.fintory.common.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ApiResultCode {

    SUCCESS(HttpStatus.OK, "SUCCESS", "요청이 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String resultCode;
    private final String message;

}
