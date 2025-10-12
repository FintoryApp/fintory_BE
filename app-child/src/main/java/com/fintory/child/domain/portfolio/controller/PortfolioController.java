package com.fintory.child.domain.portfolio.controller;

import com.fintory.auth.util.CustomUserDetails;
import com.fintory.domain.portfolio.dto.ExchangeRateResponse;
import com.fintory.domain.portfolio.dto.OwnedStockMetrics;
import com.fintory.domain.portfolio.dto.PortfolioSummary;
import com.fintory.domain.portfolio.dto.summary.PortfolioSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@Tag(name = "포트폴리오 조회 API", description = "사용자 투자 포트폴리오 조회 및 관리")
public interface PortfolioController {

    @Operation(summary = "포트폴리오 요약 정보 조회", description = "전체 투자 현황, 총 평가금액, 수익률 등 포트폴리오 요약 정보를 조회합니다")
    @ApiResponse(responseCode = "200", description = "포트폴리오 요약 정보 조회 성공")
    public ResponseEntity<com.fintory.common.api.ApiResponse<PortfolioSummary>> getPortfolioSummary();


    @Operation(summary = "현재 달러 환율 조회", description = "현재 달러 환율을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "환율 조회 성공")
    public ResponseEntity<com.fintory.common.api.ApiResponse<ExchangeRateResponse>> getExchangeRate();


    @Operation(summary = "국내 주식 투자 현황 조회", description = "국내 보유 주식에 대해 가격, 수량 및 총 보유 현금 반환")
    @ApiResponse(responseCode = "200", description = "국내 보유 주식 정보 조회 성공")
    public ResponseEntity<com.fintory.common.api.ApiResponse<PortfolioSummaryResponse>> getKoreanPortfolioSummaryResponse(
            @AuthenticationPrincipal CustomUserDetails user
    );
    
    @Operation(summary = "해외 주식 투자 현황 조회", description = "해외 보유 주식에 대해 가격, 수량 및 총 보유 현금 반환")
    @ApiResponse(responseCode = "200", description = "해외 보유 주식 정보 조회 성공")
    public ResponseEntity<com.fintory.common.api.ApiResponse<PortfolioSummaryResponse>> getOverseasPortfolioSummaryResponse(
            @AuthenticationPrincipal CustomUserDetails user
    );



}