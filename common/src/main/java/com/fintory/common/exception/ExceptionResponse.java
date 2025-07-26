package com.fintory.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ExceptionResponse {

    private String code;      // 에러 코드 문자열
    private String message;   // 사용자에게 전달할 메시지
    private String timestamp; // 에러 발생 시간 (선택)

    public ExceptionResponse(DomainErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.timestamp = LocalDateTime.now().toString(); // ISO 8601 형식 등
    }

}
