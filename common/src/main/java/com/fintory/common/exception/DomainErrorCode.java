package com.fintory.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum DomainErrorCode {

    //global
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "알 수 없는 서버 에러 발생"),
    VALIDATION_FAIL(HttpStatus.BAD_REQUEST, "VALIDATION_FAIL", "입력값이 유효하지 않습니다."),
    JSON_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JSON_PARSING_ERROR", "JSON 파싱에 실패했습니다"),

    //API
    API_RESPONSE_EMPTY(HttpStatus.SERVICE_UNAVAILABLE,"API_RESPONSE_EMPTY","API 응답 데이터가 비어있습니다"),
    API_CONNECTION_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "API_CONNECTION_ERROR", "API 연결에 실패했습니다"),
    COMPLETE_INITIALIZATION_FAILURE(HttpStatus.SERVICE_UNAVAILABLE,"COMPLETE_INITIALIZATION_FAILURE","모든 종목에 대한 초기화 작업 실패"),

    //user
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND", "유저를 찾을 수 없습니다."),
    LOGINED_USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "LOGINED_USER_NOT_FOUND", "사용자가 로그인되어 있지 않습니다" ),
    WRONG_EMAIL_OR_PASSWORD(HttpStatus.BAD_REQUEST, "WRONG_EMAIL_OR_PASSWORD", "아이디 또는 비밀번호가 일치하지 않습니다."),
    ALREADY_REGISTERED_EMAIL(HttpStatus.BAD_REQUEST, "ALREADY_REGISTERED_EMAILALREADY_REGISTERED_EMAIL", "해당 이메일로 가입된 계정이 있습니다. 다른 이메일을 사용하거나 다른 방식으로 로그인 해주세요"),

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

    //financial word
    WORD_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "WORD_NOT_FOUND", "경제 용어 불러오기 실패"),
    //account
    ACCOUNT_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR,"ACCOUNT_NOT_FOUND","계좌 불러오기 실패"),

    //ownedStock
    OWNED_STOCK_LIST_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "OWNED_STOCK_LIST_ERROR", "보유 종목 리스트 조회 중 오류 발생"),
    PORTFOLIO_CALCULATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"PORTFOLIO_CALCULATION_ERROR","포트폴리오 계산 중 오류 발생"),

    // stock price history
    STOCK_CHART_API_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "STOCK_CHART_API_UNAVAILABLE", "주식 차트 조회 실패"),
    STOCK_CHART_RESPONSE_INVALID(HttpStatus.BAD_GATEWAY, "STOCK_CHART_RESPONSE_INVALID", "주식 차트 응답 형식 에러"),
    STOCK_PRICE_HISTORY_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "STOCK_PRICE_HISTORY_SAVE_FAILED", "주식 가격 히스토리 저장에 실패"),
    STOCK_PRICE_HISTORY_FAILED(HttpStatus.NOT_FOUND,"STOCK_PRICE_HISTORY_FAILED","기간별 시세를 찾을 수 없습니다"),

    //stock
    STOCK_NOT_FOUND(HttpStatus.NOT_FOUND,"STOCK_NOT_FOUND","주식을 찾을 수 없습니다."),
    TOKEN_EMPTY(HttpStatus.NOT_FOUND,"TOKEN_EMPTY","토큰이 비어있습니다."),

    //stockRank
    STOCK_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "STOCK_DATA_NOT_FOUND", "주식 데이터를 찾을 수 없습니다"),

    //live stock price
    LIVE_STOCK_PRICE_NOT_FOUND(HttpStatus.NOT_FOUND,"LIVE_STOCK_PRICE_NOT_FOUND","현재가 데이터를 찾을 수 없습니다."),

    //order book
    ORDER_BOOK_NOT_FOUND(HttpStatus.NOT_FOUND,"ORDER_BOOK_NOT_FOUND","호가 데이터를 찾을 수 없습니다."),

    //stock token
    KIS_TOKEN_ISSUE_ERROR(HttpStatus.BAD_GATEWAY,"KIS_TOKEN_ISSUE_ERROR","KIS 토큰 발급 실패"),
    DB_TOKEN_ISSUE_ERROR(HttpStatus.BAD_GATEWAY,"DB_TOKEN_ISSUE_ERROR","DB 증권 토큰 발급 실패"),
    KIS_WEBSOCKET_TOKEN_ISSUE_ERROR(HttpStatus.BAD_GATEWAY,"KIS_WEBSOCKET_TOKEN_ISSUE_ERROR","KIS 웹소켓 토큰 발급 실패"),
    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND,"TOKEN_NOT_FOUND","Redis에 토큰이 존재하지 않습니다"),

    //webSocket
    MARKET_CLOSED(HttpStatus.BAD_REQUEST,"MARKET_CLOSED", "장이 마감되어 실시간 데이터를 제공할 수 없습니다"),
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
