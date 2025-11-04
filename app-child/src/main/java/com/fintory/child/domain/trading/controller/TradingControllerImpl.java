package com.fintory.child.domain.trading.controller;

import com.fintory.auth.util.CustomUserDetails;
import com.fintory.common.api.ApiResponse;
import com.fintory.domain.common.service.RequestMetricsService;
import com.fintory.domain.portfolio.dto.TradeRequest;
import com.fintory.domain.portfolio.service.TradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/child/trading")
public class TradingControllerImpl implements  TradingController {
    private final TradingService tradingService;
    private final RequestMetricsService requestMetricsService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> trade(@RequestBody TradeRequest tradeRequest, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String email = customUserDetails.getUsername();
        tradingService.trade(tradeRequest,email);
        requestMetricsService.incrementRequestCounter("POST", "/api/child/trading");
        return ResponseEntity.ok().body(ApiResponse.ok(null));
    }
}
