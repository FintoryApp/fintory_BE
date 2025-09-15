package com.fintory.child.domain.point.controller;

import com.fintory.auth.util.CustomUserDetails;
import com.fintory.common.api.ApiResponse;
import com.fintory.domain.account.service.AccountService;
import com.fintory.domain.child.model.Child;
import com.fintory.domain.child.service.ChildService;
import com.fintory.domain.point.dto.ExchangePointRequest;
import com.fintory.domain.point.dto.ExchangedCashResponse;
import com.fintory.domain.point.dto.PointWalletResponse;
import com.fintory.domain.point.service.PointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/child/point")
@RequiredArgsConstructor
public class PointControllerImpl implements PointController{

    private final PointService pointService;
    private final AccountService accountService;
    private final ChildService childService;


    @Override
    @PostMapping("/exchange")
    public ResponseEntity<ApiResponse<ExchangedCashResponse>> exchangePoint(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ExchangePointRequest request
    ) {
        Child child = childService.getChild(user.getUsername());
        ExchangedCashResponse response = pointService.exchangePoint(child, request.point());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Override
    @GetMapping("/get-point-transactions")
    public ResponseEntity<ApiResponse<PointWalletResponse>> getPointTransactions(CustomUserDetails user) {
        Child child = childService.getChild(user.getUsername());
        PointWalletResponse response = pointService.getPointWalletWithTransactions(child);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Override
    @GetMapping("/get-total-amount-point")
    public ResponseEntity<ApiResponse<Integer>> getTotalAmountPoint(CustomUserDetails user) {
        Child child = childService.getChild(user.getUsername());
        int response = pointService.getTotalAmount(child);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
