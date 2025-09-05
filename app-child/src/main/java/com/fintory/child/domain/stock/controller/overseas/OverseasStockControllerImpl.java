package com.fintory.child.domain.stock.controller.overseas;

import com.fintory.common.api.ApiResponse;
import com.fintory.domain.stock.dto.overseas.response.OverseasLiveStockPriceResponse;
import com.fintory.domain.stock.dto.overseas.response.OverseasRankResponse;
import com.fintory.domain.stock.dto.overseas.response.OverseasStockPriceHistoryResponse;
import com.fintory.domain.stock.service.overseas.OverseasStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/child/stock")
@Slf4j
public class OverseasStockControllerImpl implements OverseasStockController{

    private final OverseasStockService overseasStockService;

    //해외 주식 랭킹 - 시가 총액
    @Override
    @GetMapping("overseas/rankings/market-cap")
    public ResponseEntity<ApiResponse<List<OverseasRankResponse>>> getOverseasStockMarketCapTop20(){
        List<OverseasRankResponse> overseasStockMarketCapTop20s = overseasStockService.getOverseasMarketCapTop20();
        return ResponseEntity.ok(ApiResponse.ok(overseasStockMarketCapTop20s));
    }


    //해외 주식 랭킹 - 등락률
    @Override
    @GetMapping("overseas/rankings/roc")
    public ResponseEntity<ApiResponse<List<OverseasRankResponse>>> getOverseasStockROCTop20(){
        List<OverseasRankResponse> overseasStockROCTop20s = overseasStockService.getOverseasROCTop20();
        return ResponseEntity.ok(ApiResponse.ok(overseasStockROCTop20s));
    }


    //해외 주식 랭킹 - 거래량
    @Override
    @GetMapping("overseas/rankings/trading-volume")
    public ResponseEntity<ApiResponse<List<OverseasRankResponse>>> getOverseasStockTradingVolumeTop20(){
        List<OverseasRankResponse> overseasStockTradingVolumes = overseasStockService.getOverseasTradingVolumeTop20();
        return ResponseEntity.ok(ApiResponse.ok(overseasStockTradingVolumes));
    }

    //해외 주식 - 차트 데이터
    @Override
    @GetMapping("/overseas/price-history/{code}")
    public ResponseEntity<ApiResponse<OverseasStockPriceHistoryResponse>> getOverseasStockPriceHistory(@PathVariable String code){
        OverseasStockPriceHistoryResponse overseasStockPriceHistoryResponse = overseasStockService.getOverseasStockPriceHistory(code);
        return ResponseEntity.ok(ApiResponse.ok(overseasStockPriceHistoryResponse));
    }

    //해외 주식 - 현재가 데이터
    @Override
    @GetMapping("/overseas/live-price/{code}")
    public ResponseEntity<ApiResponse<OverseasLiveStockPriceResponse>> getOverseasLiveStockPrice(@PathVariable String code){
        OverseasLiveStockPriceResponse overseasLiveStockPriceResponse = overseasStockService.getLiveStockPrice(code);
        return ResponseEntity.ok(ApiResponse.ok(overseasLiveStockPriceResponse));
    }
}
