package com.fintory.child.domain.stock.controller;

import com.fintory.domain.stock.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "주식 API", description = "주식 조회 관련 API")
public interface StockController {

    @Operation(summary = "시가총액 순위 조회", description = "국내/해외 시가총액 상위 20개 종목을 조회")
    @ApiResponse(responseCode = "200", description = "시가총액 순위 조회 성공")
    ResponseEntity<com.fintory.common.api.ApiResponse<List<RankResponse>>> getMarketCapTop20(
            @Parameter(description = "통화 구분 (국내/해외)", required = true, example = "KRW")
            @NotBlank(message="통화 구분은 필수입니다.")
            @Pattern(regexp="^(KRW|USD)$",message="통화는 KRW 또는 USD만 가능합니다")
            String currency
    );

    @Operation(summary = "등락률 순위 조회", description = "국내/해외 등락률 상위 20개 종목을 조회")
    @ApiResponse(responseCode = "200", description = "등락률 순위 조회 성공")
    ResponseEntity<com.fintory.common.api.ApiResponse<List<RankResponse>>> getROCTop20(
            @Parameter(description = "통화 구분 (국내/해외)", required = true, example = "USD")
            @NotBlank(message="통화 구분은 필수입니다.")
            @Pattern(regexp="^(KRW|USD)$",message="통화는 KRW 또는 USD만 가능합니다")
            String currency
    );

    @Operation(summary = "거래량 순위 조회", description = "국내/해외 거래량 상위 20개 종목을 조회")
    @ApiResponse(responseCode = "200", description = "거래량 순위 조회 성공")
    ResponseEntity<com.fintory.common.api.ApiResponse<List<RankResponse>>> getTradingVolumeTop20(
            @Parameter(description = "통화 구분 (국내/해외)", required = true, example = "KRW")
            @NotBlank(message="통화 구분은 필수입니다.")
            @Pattern(regexp="^(KRW|USD)$",message="통화는 KRW 또는 USD만 가능합니다")
            String currency
    );

    @Operation(summary = "주식 종목 검색", description = "키워드를 통해 주식 종목을 검색")
    @ApiResponse(responseCode = "200", description = "주식 검색 성공")
    ResponseEntity<com.fintory.common.api.ApiResponse<List<StockSearchResponse>>> searchStock(
            StockSearchRequest stockSearchRequest
    );

    @Operation(summary = "기간별 시세 데이터 조회", description = "특정 종목의 기간별 시세 데이터를 조회")
    @ApiResponse(responseCode = "200", description = "시세 데이터 조회 성공")
    ResponseEntity<com.fintory.common.api.ApiResponse<StockPriceHistoryWrapper>> getOverseasStockPriceHistory(
            @Parameter(description = "주식 종목 코드", required = true, example = "AAPL")
            @NotBlank(message="주식 종목 코드는 필수입니다")
            String code
    );

    @Operation(summary = "현재가 데이터 조회", description = "특정 종목의 현재가 데이터를 조회")
    @ApiResponse(responseCode = "200", description = "현재가 데이터 조회 성공")
    ResponseEntity<com.fintory.common.api.ApiResponse<LiveStockPriceResponse>> getEachLiveStockPrice(
            @Parameter(description = "주식 종목 코드", required = true, example = "TSLA")
            @NotBlank(message="주식 종목 코드는 필수입니다")
            String code
    );
}