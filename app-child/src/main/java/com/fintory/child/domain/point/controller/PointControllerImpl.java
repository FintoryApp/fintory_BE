package com.fintory.child.domain.point.controller;

import com.fintory.auth.util.CustomUserDetails;
import com.fintory.common.api.ApiResponse;
import com.fintory.domain.account.service.AccountService;
import com.fintory.domain.child.model.Child;
import com.fintory.domain.child.service.ChildService;
import com.fintory.domain.point.dto.PointWalletResponse;
import com.fintory.domain.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/child/point")
@RequiredArgsConstructor
public class PointControllerImpl implements PointController{

    private final PointService pointService;
    private final AccountService accountService;
    private final ChildService childService;

//   TODO:
//    @Override
//    public ResponseEntity<ApiResponse<ExchangedCashResponse>> exchangePoint(CustomUserDetails user, Integer point) {
//        return null;
//    }

    @Override
    public ResponseEntity<ApiResponse<PointWalletResponse>> getPointTransactions(CustomUserDetails user) {
        Child child = childService.getChild(user.getUsername());
        PointWalletResponse response = pointService.getPointWalletWithTransactions(child);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
