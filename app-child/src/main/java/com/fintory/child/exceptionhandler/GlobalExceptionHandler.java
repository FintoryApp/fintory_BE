package com.fintory.child.exceptionhandler;


import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, List<String>> errorMap = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String field = error.getField();
            String message = error.getDefaultMessage();
            errorMap.computeIfAbsent(field, key -> new ArrayList<>()).add(message);
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse(DomainErrorCode.VALIDATION_FAIL, errorMap));
    }

    //RequestParam, PathVariable 사용시 클래스 단위의 @validated에서 ConstraintViolationException가 터짐
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponse> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse(DomainErrorCode.VALIDATION_FAIL, message));
    }

    // Spring 6.2+에서 RequestParam, PathVariable 유효성 검사 실패 시
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ExceptionResponse> handleHandlerMethodValidation(HandlerMethodValidationException ex) {
        String message = ex.getParameterValidationResults() // 변경된 메서드
                .stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse(DomainErrorCode.VALIDATION_FAIL, message));
    }

    // Prometheus scrape 중 클라이언트 연결 끊김 처리
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public ResponseEntity<Void> handleAsyncRequestNotUsable(AsyncRequestNotUsableException ex
            , HttpServletRequest request) {
        if(request.getRequestURI().startsWith("/actuator")){
            log.debug("Prometheus scrape 중 클라이언트 연결 끊김 (무시): {}", request.getRequestURI());
            return ResponseEntity.ok().build();
        }

        log.error("Async request error",ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleUnhandledException(Exception e, HttpServletRequest request) {

        if (request.getRequestURI().startsWith("/actuator")) {
            log.debug("Actuator 엔드포인트 에러 무시: {}", request.getRequestURI());
            return ResponseEntity.ok().build();
        }

        log.error("Unknown server error", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionResponse(DomainErrorCode.INTERNAL_SERVER_ERROR));
    }


}
