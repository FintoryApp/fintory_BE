package com.fintory.child.domain.account.controller;

import com.fintory.auth.util.CustomUserDetails;
import com.fintory.common.api.ApiResponse;
import com.fintory.domain.account.dto.response.DepositTransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "계좌 API")
public interface AccountController {

    @Operation(summary = "현금 거래 전체 내역 반환")
    ResponseEntity<ApiResponse<List<DepositTransactionResponse>>> getDepositTransactionList(@AuthenticationPrincipal CustomUserDetails user);

    @Operation(summary = "총 보유 현금 반환")
    ResponseEntity<ApiResponse<BigDecimal>> getTotalCash(@AuthenticationPrincipal CustomUserDetails user);
}
