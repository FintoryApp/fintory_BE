package com.fintory.child.domain.point.controller;

import com.fintory.auth.util.CustomUserDetails;
import com.fintory.common.api.ApiResponse;
import com.fintory.domain.point.dto.PointWalletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

public interface PointController {
//   TODO:
//    // 포인트 환전
//    ResponseEntity<ApiResponse<ExchangedCashResponse>> exchangePoint(@AuthenticationPrincipal CustomUserDetails user, Integer point);

     //포인트 내역 전체 반환
     ResponseEntity<ApiResponse<PointWalletResponse>> getPointTransactions(@AuthenticationPrincipal CustomUserDetails user);
}
