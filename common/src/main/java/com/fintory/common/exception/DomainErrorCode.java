package com.fintory.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum DomainErrorCode {

    //global
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "알 수 없는 서버 에러 발생"),
    VALIDATION_FAIL(HttpStatus.BAD_REQUEST, "VALIDATION_FAIL", "입력값이 유효하지 않습니다."),

    //user
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND", "유저를 찾을 수 없습니다."),

    //news
    NEWS_LINK_GET_FAILED(HttpStatus.BAD_GATEWAY, "NEWS_LINK_GET_FAILED", "최신 뉴스 기사 링크 불러오기 실패"),
    NEWS_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "NEWS_NOT_FOUND", "저장된 뉴스 기사 불러오기 실패"),
    NEWS_CRAWLING_FAILED(HttpStatus.BAD_GATEWAY, "NEWS_CRAWLING_FAILED", "뉴스 기사 크롤링 실패");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    DomainErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
