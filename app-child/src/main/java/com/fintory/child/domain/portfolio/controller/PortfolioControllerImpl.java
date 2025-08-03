package com.fintory.child.domain.portfolio.controller;


import com.fintory.common.api.ApiResponse;
import com.fintory.domain.portfolio.dto.OwnedStockList;
import com.fintory.domain.portfolio.dto.PortfolioSummary;
import com.fintory.domain.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/portfolio")
@Slf4j
public class PortfolioControllerImpl implements PortfolioController {

    private final PortfolioService portfolioService;

    @Override
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<PortfolioSummary>> getPortfolioSummary() {
        PortfolioSummary portfolioSummary = portfolioService.getPortfolioSummary();
        return ResponseEntity.ok(ApiResponse.ok(portfolioSummary));
    }

    @Override
    @GetMapping("/stocks")
    public ResponseEntity<ApiResponse<List<OwnedStockList>>> getOwnedStockList() {
        List<OwnedStockList> ownedStockList = portfolioService.getOwnedStockList();
        return ResponseEntity.ok(ApiResponse.ok(ownedStockList));
    }
}