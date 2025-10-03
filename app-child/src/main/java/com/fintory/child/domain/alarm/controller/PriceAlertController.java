package com.fintory.child.domain.alarm.controller;

import com.fintory.auth.util.CustomUserDetails;
import com.fintory.common.api.ApiResponse;
import com.fintory.domain.alarm.dto.PriceAlertRequest;
import com.fintory.domain.alarm.dto.PriceAlertResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "감시가 지정 API")
public interface PriceAlertController {

    @Operation(summary = "감시가 생성")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "감시가 생성 성공")
    ResponseEntity<ApiResponse<Void>> createPriceAlert(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody PriceAlertRequest request
    );

    @Operation(summary = "감시가 목록 조회")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "감시가 목록 반환")
    ResponseEntity<ApiResponse<List<PriceAlertResponse>>> getPriceAlerts(
            @PathVariable String stockCode,
            @AuthenticationPrincipal CustomUserDetails user
    );

    @Operation(summary = "감시가 삭제")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "감시가 삭제 성공")
    ResponseEntity<ApiResponse<Void>> deletePriceAlert(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user
    );
}