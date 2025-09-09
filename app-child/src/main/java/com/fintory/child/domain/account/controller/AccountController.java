package com.fintory.child.domain.account.controller;

import com.fintory.auth.util.CustomUserDetails;
import com.fintory.common.api.ApiResponse;
import com.fintory.domain.account.dto.response.DepositTransactionResponse;
import com.fintory.domain.account.dto.response.TotalAssetsResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@Tag(name = "계좌 API")
public interface AccountController {

    // TODO: list depositTransaction 가져오기 (포인트든, 주식이든 거래시 현금 내역이 업데이트되는 로직 작성해야됨 -> 그랬으면 그냥 현금내역만 가져오면 됨)
    ResponseEntity<ApiResponse<List<DepositTransactionResponse>>> getDepositTransactionList(@AuthenticationPrincipal CustomUserDetails user);

    // TODO: 총 재산 가져오기 -> account.total 만 가져오면 됨
    ResponseEntity<ApiResponse<TotalAssetsResponse>> getTotalAssets(@AuthenticationPrincipal CustomUserDetails user);


}
