package com.fintory.child.domain.trading;

import com.fintory.auth.util.CustomUserDetails;
import com.fintory.common.api.ApiResponse;
import com.fintory.domain.portfolio.dto.TradeRequest;
import com.fintory.domain.stock.service.TradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/child/trading")
public class TradingControllerImpl {
    private final TradingService tradingService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> trade(@RequestBody TradeRequest tradeRequest, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String email = customUserDetails.getUsername();
        tradingService.trade(tradeRequest,email);
        return ResponseEntity.ok().body(ApiResponse.ok(null));
    }
}
