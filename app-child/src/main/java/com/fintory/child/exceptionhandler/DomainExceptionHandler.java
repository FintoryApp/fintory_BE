package com.fintory.child.exceptionhandler;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.common.exception.ExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class DomainExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ExceptionResponse> customExceptionHandler(DomainException e) {

        DomainErrorCode errorCode = e.getErrorCode();

        log.warn("DomainException code: {}, message: {}", errorCode.getCode(), errorCode.getMessage());

        ExceptionResponse exceptionResponse = new ExceptionResponse(errorCode);
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(exceptionResponse);
    }
}
