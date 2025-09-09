package com.fintory.child.domain.account.controller;

import com.fintory.auth.util.CustomUserDetails;
import com.fintory.common.api.ApiResponse;
import com.fintory.domain.account.dto.response.DepositTransactionResponse;
import com.fintory.domain.account.dto.response.TotalAssetsResponse;
import com.fintory.domain.account.service.AccountService;
import com.fintory.domain.child.model.Child;
import com.fintory.domain.child.service.ChildService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/child/account")
@RequiredArgsConstructor
public class AccountControllerImpl implements AccountController{

    private final AccountService accountService;
    private final ChildService childService;


    @Override
    @GetMapping("/deposit-transaction-list")
    public ResponseEntity<ApiResponse<List<DepositTransactionResponse>>> getDepositTransactionList(CustomUserDetails user) {

        Child child = childService.getChild(user.getUsername());
        List<DepositTransactionResponse> list = accountService.getDepositTransactionsByChild(child);

        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @Override
    @GetMapping("/total-assets")
    public ResponseEntity<ApiResponse<TotalAssetsResponse>> getTotalAssets(CustomUserDetails user) {

        Child child = childService.getChild(user.getUsername());
        TotalAssetsResponse response = accountService.getTotalAssets(child);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
