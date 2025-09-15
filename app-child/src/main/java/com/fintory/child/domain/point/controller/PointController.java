package com.fintory.child.domain.point.controller;

import com.fintory.auth.util.CustomUserDetails;
import com.fintory.common.api.ApiResponse;
import com.fintory.domain.point.dto.ExchangePointRequest;
import com.fintory.domain.point.dto.ExchangedCashResponse;
import com.fintory.domain.point.dto.PointWalletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

public interface PointController {

     // 포인트 환전
     ResponseEntity<ApiResponse<ExchangedCashResponse>> exchangePoint(
             @AuthenticationPrincipal CustomUserDetails user,
             @Valid @RequestBody ExchangePointRequest request
     );

     //포인트 내역 전체 반환
     ResponseEntity<ApiResponse<PointWalletResponse>> getPointTransactions(@AuthenticationPrincipal CustomUserDetails user);

     ResponseEntity<ApiResponse<Integer>> getTotalAmountPoint(@AuthenticationPrincipal CustomUserDetails user);
}
