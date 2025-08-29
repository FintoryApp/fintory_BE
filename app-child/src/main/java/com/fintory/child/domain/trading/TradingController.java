package com.fintory.child.domain.trading;

import com.fintory.auth.util.CustomUserDetails;
import com.fintory.domain.portfolio.dto.TradeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name="주식 거래 API", description="주식 매도/매수 거래 관리")
public interface TradingController {

    @Operation(summary="주식 거래 처리", description="주식 매수 또는 매도 거래를 처리합니다")
    @ApiResponse(responseCode = "200",description="주식 거래 처리 성공")
    public ResponseEntity<com.fintory.common.api.ApiResponse<Void>> trade(
            @Valid @RequestBody TradeRequest tradeRequest,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
            );

}
