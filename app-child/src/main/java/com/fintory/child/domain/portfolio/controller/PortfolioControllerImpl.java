package com.fintory.child.domain.portfolio.controller;


import com.fintory.auth.util.CustomUserDetails;
import com.fintory.common.api.ApiResponse;
import com.fintory.domain.child.model.Child;
import com.fintory.domain.child.service.ChildService;
import com.fintory.domain.portfolio.dto.OwnedStockMetrics;
import com.fintory.domain.portfolio.dto.PortfolioSummary;
import com.fintory.domain.portfolio.dto.summary.PortfolioSummaryResponse;
import com.fintory.domain.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/child/portfolio")
@Slf4j
public class PortfolioControllerImpl implements PortfolioController {

    private final PortfolioService portfolioService;
    private final ChildService childService;

    @Override
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<PortfolioSummary>> getPortfolioSummary() {
        PortfolioSummary portfolioSummary = portfolioService.getPortfolioSummary();
        return ResponseEntity.ok(ApiResponse.ok(portfolioSummary));
    }

    @Override
    @GetMapping("/stocks")
    public ResponseEntity<ApiResponse<List<OwnedStockMetrics>>> getOwnedStockList() {
        List<OwnedStockMetrics> ownedStockMetrics = portfolioService.getOwnedStockMetrics();
        return ResponseEntity.ok(ApiResponse.ok(ownedStockMetrics));
    }

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<PortfolioSummaryResponse>> getPortfolioSummaryResponse(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Child child = childService.getChild(user.getUsername());
        PortfolioSummaryResponse response = portfolioService.getPortfolioSummaryResponseByChild(child);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}