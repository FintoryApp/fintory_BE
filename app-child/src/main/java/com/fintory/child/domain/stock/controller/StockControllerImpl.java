package com.fintory.child.domain.stock.controller;

import com.fintory.common.api.ApiResponse;
import com.fintory.domain.stock.dto.*;
import com.fintory.domain.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/child/stock")
@RequiredArgsConstructor
@Slf4j
public class StockControllerImpl implements StockController {

    private final StockService stockService;

    @Override
    @GetMapping("/market-cap/top20")
    public ResponseEntity<ApiResponse<List<RankResponse>>> getMarketCapTop20(
            @RequestParam String currency
    ) {
        List<RankResponse> rankList = stockService.getMarketCapTop20(currency);
        return ResponseEntity.ok(ApiResponse.ok(rankList));
    }

    //TODO ROC rate 반올림 수정. rank가 동일할 시 해결
    @Override
    @GetMapping("/roc/top20")
    public ResponseEntity<ApiResponse<List<RankResponse>>> getROCTop20(
            @RequestParam String currency
    ) {
        List<RankResponse> rankList = stockService.getROCTop20(currency);
        return ResponseEntity.ok(ApiResponse.ok(rankList));
    }

    @Override
    @GetMapping("/volume/top20")
    public ResponseEntity<ApiResponse<List<RankResponse>>> getTradingVolumeTop20(
            @RequestParam String currency
    ) {
        List<RankResponse> rankList = stockService.getTradingVolumeTop20(currency);
        return ResponseEntity.ok(ApiResponse.ok(rankList));
    }

    @Override
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<StockSearchResponse>>> searchStock(
            @RequestBody StockSearchRequest stockSearchRequest
    ) {
        List<StockSearchResponse> searchResults = stockService.searchStock(stockSearchRequest);
        return ResponseEntity.ok(ApiResponse.ok(searchResults));
    }

    //NOTE 국내 주식 -> code + .XKRX 형식. 어차피 백엔드에서 code 값을 직접 전달하므로 문제x

    @Override
    @GetMapping("/{code}/price-history")
    public ResponseEntity<ApiResponse<StockPriceHistoryWrapper>> getOverseasStockPriceHistory(
            @PathVariable String code
    ) {
        StockPriceHistoryWrapper priceHistory = stockService.getOverseasStockPriceHistory(code);
        return ResponseEntity.ok(ApiResponse.ok(priceHistory));
    }

    @Override
    @GetMapping("/{code}/live-price")
    public ResponseEntity<ApiResponse<LiveStockPriceResponse>> getEachLiveStockPrice(
            @PathVariable String code
    ) {
        LiveStockPriceResponse livePrice = stockService.getEachLiveStockPrice(code);
        return ResponseEntity.ok(ApiResponse.ok(livePrice));
    }
}
