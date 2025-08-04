package com.fintory.child.domain.portfolio.controller;

import com.fintory.domain.portfolio.dto.OwnedStockMetrics;
import com.fintory.domain.portfolio.dto.PortfolioSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "포트폴리오 조회 API", description = "사용자 투자 포트폴리오 조회 및 관리")
public interface PortfolioController {

    @Operation(summary = "포트폴리오 요약 정보 조회", description = "전체 투자 현황, 총 평가금액, 수익률 등 포트폴리오 요약 정보를 조회합니다")
    @ApiResponse(responseCode = "200", description = "포트폴리오 요약 정보 조회 성공")
    public ResponseEntity<com.fintory.common.api.ApiResponse<PortfolioSummary>> getPortfolioSummary();

    @Operation(summary = "보유 주식 목록 조회", description = "현재 보유 중인 모든 주식의 상세 정보를 조회합니다")
    @ApiResponse(responseCode = "200", description = "보유 주식 목록 조회 성공")
    public ResponseEntity<com.fintory.common.api.ApiResponse<List<OwnedStockMetrics>>> getOwnedStockList();
}