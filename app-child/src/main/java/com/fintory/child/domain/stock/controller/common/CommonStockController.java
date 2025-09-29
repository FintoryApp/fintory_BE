package com.fintory.child.domain.stock.controller.common;

import com.fintory.domain.stock.dto.korean.response.StockSearchResponse;
import com.fintory.domain.stock.dto.websocket.MarketStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface CommonStockController {

    @Operation(summary = "주식 종목 검색",description = "주식 종목 코드 혹은 이름을 입력하여 주식 종목 검색")
    @ApiResponse(responseCode="200", description="주식 종목 검색 성공")
    public ResponseEntity<com.fintory.common.api.ApiResponse<List<StockSearchResponse>>> searchStock(@RequestParam String keyword);

    @Operation(summary = "장 열림 여부 확인", description = "현재 국내/해외 주식장이 열려 있는지 상태를 반환(korean/overseas/no).")
    @ApiResponse(responseCode = "200", description = "장 상태 조회 성공")
    public ResponseEntity<com.fintory.common.api.ApiResponse<MarketStatusResponse>> getMarketStatus();



}
