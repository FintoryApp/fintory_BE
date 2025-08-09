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
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "DUPLICATE_EMAIL", "이미 존재하는 이메일입니다."),
    LOGINED_USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "LOGINED_USER_NOT_FOUND", "사용자가 로그인되어 있지 않습니다" ),
    WRONG_EMAIL_OR_PASSWORD(HttpStatus.BAD_REQUEST, "WRONG_EMAIL_OR_PASSWORD", "아이디 또는 비밀번호가 일치하지 않습니다."),

    //jwt
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_TOKEN", "만료된 토큰입니다."),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "UNSUPPORTED_TOKEN", "지원하지 않는 토큰입니다."),
    EMPTY_TOKEN(HttpStatus.UNAUTHORIZED, "EMPTY_TOKEN", "토큰이 존재하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "유효하지 않은 리프레쉬 토큰입니다."),
    INVALID_TOKEN_TYPE(HttpStatus.BAD_REQUEST, "INVALID_TOKEN_TYPE", "토큰 카테고리가 일치하지 않습니다"),

    //news
    NEWS_LINK_GET_FAILED(HttpStatus.BAD_GATEWAY, "NEWS_LINK_GET_FAILED", "최신 뉴스 기사 링크 불러오기 실패"),
    NEWS_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "NEWS_NOT_FOUND", "저장된 뉴스 기사 불러오기 실패"),
    NEWS_CRAWLING_FAILED(HttpStatus.BAD_GATEWAY, "NEWS_CRAWLING_FAILED", "뉴스 기사 크롤링 실패"),

    //account
    ACCOUNT_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR,"ACCOUNT_NOT_FOUND","계좌 불러오기 실패"),

    //ownedStock
    OWNED_STOCK_LIST_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "OWNED_STOCK_LIST_ERROR", "보유 종목 리스트 조회 중 오류 발생"),
    PORTFOLIO_CALCULATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"PORTFOLIO_CALCULATION_ERROR","포트폴리오 계산 중 오류 발생");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    DomainErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
