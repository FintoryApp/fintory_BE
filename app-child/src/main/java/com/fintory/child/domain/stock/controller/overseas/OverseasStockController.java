package com.fintory.child.domain.stock.controller.overseas;

import com.fintory.domain.stock.dto.overseas.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(name="해외 주식 조회 API", description="해외 주식 종목 및 정보(호가, 순위, 차트 등) 조회 관련 API")
public interface OverseasStockController {

    @Operation(summary="해외 주식 시가 총액 순위 조회", description = "해외 주식 시가 총액 순위 조회")
    @ApiResponse(responseCode="200", description="해외 주식 시가 총액 순위 조회 성공")
    public ResponseEntity<com.fintory.common.api.ApiResponse<List<OverseasMarketCapResponse>>> getOverseasStockMarketCapTop20();

    @Operation(summary="해외 주식 상승률 순위 조회", description = "해외 주식 상승률 순위 조회")
    @ApiResponse(responseCode="200", description="해외 주식 상승률 순위 조회 성공")
    public ResponseEntity<com.fintory.common.api.ApiResponse<List<OverseasROCResponse>>> getOverseasStockROCTop20();

    @Operation(summary="해외 주식 거래량 순위 조회", description = "해외 주식 거래량 순위 조회")
    @ApiResponse(responseCode="200", description="해외 주식 거래량 순위 조회 성공")
    public ResponseEntity<com.fintory.common.api.ApiResponse<List<OverseasRankResponse>>> getOverseasStockTradingVolumeTop20();

    @Operation(summary="해외 주식 차트 조회", description="특정 해외 주식//의 기간별 시세 조회")
    @ApiResponse(responseCode="200", description="해외 주식 차트 조회 성공")
    public ResponseEntity<com.fintory.common.api.ApiResponse<OverseasStockPriceHistoryResponse>> getOverseasStockPriceHistory
            (@PathVariable
             @NotBlank(message = "종목 코드는 필수입니다.")
             @Pattern(regexp="^[A-Z]{1,4}$", message = "미국 종목 코드는 1-4자리 대문자여야 합니다.")
             String code);

    @Operation(summary="해외 주식 현재가 조회", description="특정 해외 주식의 현재가, 변동가격, 변화율 조회")
    @ApiResponse(responseCode="200", description="해외 주식 현재가 조회 성공")
    public ResponseEntity<com.fintory.common.api.ApiResponse<OverseasLiveStockPriceResponse>> getOverseasLiveStockPrice
            (@PathVariable
             @NotBlank(message = "종목 코드는 필수입니다.")
             @Pattern(regexp="^[A-Z]{1,4}$", message = "미국 종목 코드는 1-4자리 대문자여야 합니다.")
             String code);

}