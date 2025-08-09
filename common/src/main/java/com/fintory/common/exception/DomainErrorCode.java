package com.fintory.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum DomainErrorCode {

    //global
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "알 수 없는 서버 에러 발생"),
    VALIDATION_FAIL(HttpStatus.BAD_REQUEST, "VALIDATION_FAIL", "입력값이 유효하지 않습니다."),
    API_RESPONSE_EMPTY(HttpStatus.SERVICE_UNAVAILABLE,"API_RESPONSE_EMPTY","API 응답 데이터가 비어있습니다"),

    //user
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND", "유저를 찾을 수 없습니다."),

    //news
    NEWS_LINK_GET_FAILED(HttpStatus.BAD_GATEWAY, "NEWS_LINK_GET_FAILED", "최신 뉴스 기사 링크 불러오기 실패"),
    NEWS_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "NEWS_NOT_FOUND", "저장된 뉴스 기사 불러오기 실패"),
    NEWS_CRAWLING_FAILED(HttpStatus.BAD_GATEWAY, "NEWS_CRAWLING_FAILED", "뉴스 기사 크롤링 실패"),

    //account
    ACCOUNT_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR,"ACCOUNT_NOT_FOUND","계좌 불러오기 실패"),

    //ownedStock
    OWNED_STOCK_LIST_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "OWNED_STOCK_LIST_ERROR", "보유 종목 리스트 조회 중 오류 발생"),
    PORTFOLIO_CALCULATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"PORTFOLIO_CALCULATION_ERROR","포트폴리오 계산 중 오류 발생"),

    // stock price history
    STOCK_CHART_API_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "STOCK_CHART_API_UNAVAILABLE", "주식 차트 조회 실패"),
    STOCK_CHART_RESPONSE_INVALID(HttpStatus.BAD_GATEWAY, "STOCK_CHART_RESPONSE_INVALID", "주식 차트 응답 형식 에러"),
    STOCK_PRICE_HISTORY_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "STOCK_PRICE_HISTORY_SAVE_FAILED", "주식 가격 히스토리 저장에 실패"),

    //stock
    STOCK_NOT_FOUND(HttpStatus.NOT_FOUND,"STOCK_NOT_FOUND","주식을 찾을 수 없습니다."),

    //stockRank
    STOCK_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "STOCK_DATA_NOT_FOUND", "주식 데이터를 찾을 수 없습니다"),

    //live stock price
    LIVE_STOCK_PRICE_NOT_FOUND(HttpStatus.NOT_FOUND,"LIVE_STOCK_PRICE_NOT_FOUND","현재가 데이터를 찾을 수 없습니다."),

    //order book
    ORDER_BOOK_NOT_FOUND(HttpStatus.NOT_FOUND,"ORDER_BOOK_NOT_FOUND","호가 데이터를 찾을 수 없습니다."),

    //stock token
    KIS_TOKEN_ISSUE_ERROR(HttpStatus.BAD_GATEWAY,"KIS_TOKEN_ISSUE_ERROR","KIS 토큰 발급 실패"),
    KIS_WEBSOCKET_TOKEN_ISSUE_ERROR(HttpStatus.BAD_GATEWAY,"KIS_WEBSOCKET_TOKEN_ISSUE_ERROR","KIS 웹소켓 토큰 발급 실패"),

    //webSocket
    STOCK_SUBSCRIBE_FAILED(HttpStatus.SERVICE_UNAVAILABLE,"STOCK_SUBSCRIBE_FAILED","웹소켓 구독 요청에 실패했습니다"),
    STOCK_UNSUBSCRIBE_FAILED(HttpStatus.SERVICE_UNAVAILABLE,"STOCK_UNSUBSCRIBE_FAILED","웹소켓 구독 취소 요청에 실패했습니다"),
    WEBSOCKET_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "WEBSOCKET_SEND_FAILED","웹소켓 메시지 전송에 실패했습니다."),
    WEBSOCKET_CONNECTION_FAILED(HttpStatus.SERVICE_UNAVAILABLE,"WEBSOCKET_CONNECTION_FAILED","웹소켓 연결에 실패했습니다."),
    WEBSOCKET_MESSAGE_PARSE_FAILED(HttpStatus.BAD_REQUEST,"WEBSOCKET_MESSAGE_PARSE_FAILED","웹소켓 메시지 파싱에 실패했습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    DomainErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
