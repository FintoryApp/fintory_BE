package com.fintory.child.domain.point.controller;

import com.fintory.auth.util.CustomUserDetails;
import com.fintory.common.api.ApiResponse;
import com.fintory.domain.point.dto.PointWalletResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "포인트 API")
public interface PointController {

     // 포인트 환전
//     @Operation(summary = "포인트 환전")
//     ResponseEntity<ApiResponse<ExchangedCashResponse>> exchangePoint(@AuthenticationPrincipal CustomUserDetails user, BigDecimal point);

     //포인트 내역 전체 반환
     @Operation(summary = "포인트 거래내역 반환")
     ResponseEntity<ApiResponse<PointWalletResponse>> getPointTransactions(@AuthenticationPrincipal CustomUserDetails user);

     @Operation(summary = "총 보유 포인트 조회")
     ResponseEntity<ApiResponse<Integer>> getTotalAmountPoint(@AuthenticationPrincipal CustomUserDetails user);
}
