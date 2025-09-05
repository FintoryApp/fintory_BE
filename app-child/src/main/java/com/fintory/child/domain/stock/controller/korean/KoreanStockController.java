package com.fintory.child.domain.stock.controller.korean;

import com.fintory.domain.stock.dto.korean.response.KoreanLiveStockPriceResponse;
import com.fintory.domain.stock.dto.korean.response.KoreanRankResponse;
import com.fintory.domain.stock.dto.korean.response.KoreanStockPriceHistoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import io.swagger.v3.oas.annotations.responses.ApiResponse;


import java.util.List;

@Tag(name="주식 종목 조회 API", description="주식 종목 및 정보(현재가, 호가, 주식기본정보 등) 조회 관련 API")
public interface KoreanStockController {

    @Operation(summary="국내 주식 시가 총액 순위 조회", description = "국내 주식 시가 총액 순위 조회")
    @ApiResponse(responseCode="200", description="국내 주식 시가 총액 순위 조회 성공")
    public ResponseEntity<com.fintory.common.api.ApiResponse<List<KoreanRankResponse>>> getKoreanStockMarketCapTop20();

    @Operation(summary="국내 주식 상승률 순위 조회", description = "국내 주식 상승률 순위 조회")
    @ApiResponse(responseCode="200", description="국내 주식 상승률 순위 조회 성공")
    public ResponseEntity<com.fintory.common.api.ApiResponse<List<KoreanRankResponse>>> getKoreanStockROCTop20();

    @Operation(summary="국내 주식 거래량 순위 조회", description = "국내 주식 거래량 순위 조회")
    @ApiResponse(responseCode="200", description="국내 주식 거래량 순위 조회 성공")
    public ResponseEntity<com.fintory.common.api.ApiResponse<List<KoreanRankResponse>>> getKoreanStockTradingVolumeTop20();

    @Operation(summary="국내 주식 차트 조회", description="특정 국내 주식의 기간별 시세 조회")
    @ApiResponse(responseCode="200", description="국내 주식 차트 조회 성공")
    public ResponseEntity<com.fintory.common.api.ApiResponse<KoreanStockPriceHistoryResponse>> getKoreanStockDetailInfo
            (@PathVariable
             @NotBlank(message = "종목 코드는 필수입니다.") @Pattern(regexp="^[0-9]{6}$", message="종목 코드는 6자리 숫자여야 합니다.")
             String code);

    @Operation(summary="국내 주식 현재가 조회", description="특정 국내 주식의 현재가, 변동가격, 변화율 조회")
    @ApiResponse(responseCode="200", description="국내 주식 현재가 조회 성공")
    public ResponseEntity<com.fintory.common.api.ApiResponse<KoreanLiveStockPriceResponse>> getKoreanStockLivePrice
            (@PathVariable
             @NotBlank(message = "종목 코드는 필수입니다.")
             @Pattern(regexp="^[0-9]{6}$", message="종목 코드는 6자리 숫자여야 합니다.")
             String code);
}