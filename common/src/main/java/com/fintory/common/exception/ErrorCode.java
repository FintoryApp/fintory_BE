package com.fintory.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    VALIDATION_FAIL(HttpStatus.BAD_REQUEST, "VALIDATION_FAIL", "입력값이 유효하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND", "유저를 찾을 수 없습니다."),
    NEWS_NOT_FOUND(HttpStatus.BAD_REQUEST, "NEWS_NOT_FOUND", "뉴스를 찾을 수 없습니다"),

    TOKEN_REQUEST_FAILED(HttpStatus.BAD_REQUEST,"TOKEN_REQUEST_FAILED","액세스 토큰 발급 중 오류가 발생했습니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
