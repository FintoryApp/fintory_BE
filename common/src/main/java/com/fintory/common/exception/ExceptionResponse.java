package com.fintory.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionResponse {

    private String code;      // 에러 코드 문자열
    private String message;   // 사용자에게 전달할 메시지
    private String timestamp; // 에러 발생 시간
    private Map<String, List<String>> errors; // 필드별 에러 메시지 (선택)

    // 일반 에러용 생성자
    public ExceptionResponse(DomainErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.timestamp = LocalDateTime.now().toString();
    }

    // 사용자 정의 메시지용
    public ExceptionResponse(DomainErrorCode errorCode, String message) {
        this.code = errorCode.getCode();
        this.message = message;
        this.timestamp = LocalDateTime.now().toString();
    }

    // 필드 검증 에러용
    public ExceptionResponse(DomainErrorCode errorCode, Map<String, List<String>> errors) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.timestamp = LocalDateTime.now().toString();
        this.errors = errors;
    }
}
