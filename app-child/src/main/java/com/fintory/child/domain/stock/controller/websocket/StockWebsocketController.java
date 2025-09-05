package com.fintory.child.domain.stock.controller.websocket;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

public interface StockWebsocketController {

    /* 개별 구독 */
    @Operation(summary = "한국 주식 실시간 구독", description = "한국 주식 종목의 실시간 가격 정보를 구독합니다.")
    @ApiResponse(responseCode = "200", description = "한국 주식 구독 성공")
    ResponseEntity<com.fintory.common.api.ApiResponse<String>> subscribeKoreanStock(
            @Parameter(description = "종목 코드", example = "005930") @PathVariable String code);

    @Operation(summary = "한국 주식 실시간 구독 해제", description = "한국 주식 종목의 실시간 가격 정보 구독을 해제합니다")
    @ApiResponse(responseCode = "200", description = "한국 주식 구독 해제 성공")
    ResponseEntity<com.fintory.common.api.ApiResponse<String>> unsubscribeKoreanStock(
            @Parameter(description = "종목 코드", example = "005930") @PathVariable String code);

    @Operation(summary = "해외 주식 실시간 구독", description = "해외 주식 종목의 실시간 가격 정보를 구독합니다.")
    @ApiResponse(responseCode = "200", description = "해외 주식 구독 성공")
    ResponseEntity<com.fintory.common.api.ApiResponse<String>> subscribeOverseasStock(
            @Parameter(description = "종목 코드", example = "AAPL") @PathVariable String code);

    @Operation(summary = "해외 주식 실시간 구독 해제", description = "해외 주식 종목의 실시간 가격 정보 구독을 해제합니다")
    @ApiResponse(responseCode = "200", description = "해외 주식 구독 해제 성공")
    ResponseEntity<com.fintory.common.api.ApiResponse<String>> unsubscribeOverseasStock(
            @Parameter(description = "종목 코드", example = "AAPL") @PathVariable String code);


    /* 전체 구독 */
    @Operation(summary = "한국 주식 전체 구독", description = "모든 한국 주식을 일괄 구독합니다")
    @ApiResponse(responseCode = "200", description = "구독 시작")
    ResponseEntity<com.fintory.common.api.ApiResponse<String>> subscribeAllKoreanStocks();

    @Operation(summary = "한국 주식 전체 구독 해제", description = "모든 한국 주식 구독을 해제합니다")
    @ApiResponse(responseCode = "200", description = "구독 해제 시작")
    ResponseEntity<com.fintory.common.api.ApiResponse<String>> unsubscribeAllKoreanStocks();

    @Operation(summary = "해외 주식 전체 구독", description = "모든 해외 주식을 일괄 구독합니다")
    @ApiResponse(responseCode = "200", description = "구독 시작")
    ResponseEntity<com.fintory.common.api.ApiResponse<String>> subscribeAllOverseasStocks();

    @Operation(summary = "해외 주식 전체 구독 해제", description = "모든 해외 주식 구독을 해제합니다")
    @ApiResponse(responseCode = "200", description = "구독 해제 시작")
    ResponseEntity<com.fintory.common.api.ApiResponse<String>> unsubscribeAllOverseasStocks();

}
