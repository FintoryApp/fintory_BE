package com.fintory.child.domain.stock.controller.korean;

import com.fintory.common.api.ApiResponse;

import com.fintory.domain.stock.dto.korean.response.*;
import com.fintory.domain.stock.service.korean.KoreanStockService;
import com.fintory.infra.domain.stock.service.korean.KoreanLiveStockPriceServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/child/stock")
@Slf4j
public class KoreanStockControllerImpl implements KoreanStockController {

    private final KoreanStockService stockService;
    private final KoreanLiveStockPriceServiceImpl liveStockPriceService;

    //국내 주식 랭킹 - 시가 총액
    @Override
    @GetMapping("korean/rankings/market-cap")
    public ResponseEntity<ApiResponse<List<KoreanMarketCapResponse>>> getKoreanStockMarketCapTop20(){
        List<KoreanMarketCapResponse> koreanStockMarketCapTop20s = stockService.getKoreanMarketCapTop20();
//        log.info("koreanStockMarket"+koreanStockMarketCapTop20s.toString());
        return ResponseEntity.ok(ApiResponse.ok(koreanStockMarketCapTop20s));
    }

    //국내 주식 랭킹 - 등락률
    @Override
    @GetMapping("korean/rankings/roc")
    public ResponseEntity<ApiResponse<List<KoreanROCResponse>>> getKoreanStockROCTop20(){
        List<KoreanROCResponse> koreanStockROCTop20s = stockService.getKoreanROCTop20();
        return ResponseEntity.ok(ApiResponse.ok(koreanStockROCTop20s));
    }

    //국내 주식 랭킹 - 거래량
    @Override
    @GetMapping("korean/rankings/tradingVolume")
    public ResponseEntity<ApiResponse<List<KoreanRankResponse>>> getKoreanStockTradingVolumeTop20(){
        List<KoreanRankResponse> koreanStockTradingVolumes = stockService.getKoreanTradingVolumeTop20();
        return ResponseEntity.ok(ApiResponse.ok(koreanStockTradingVolumes));
    }

    //국내 주식 차트 데이터
    @Override
    @GetMapping("/korean/stockPriceHistory/{code}")
    public ResponseEntity<ApiResponse<KoreanStockPriceHistoryResponse>> getKoreanStockDetailInfo(@PathVariable String code){
        KoreanStockPriceHistoryResponse stockPriceHistory = stockService.getKoreanStockPriceHistory(code);
        return  ResponseEntity.ok(ApiResponse.ok(stockPriceHistory));
    }

    //국내 주식 - 현재가 데이터
    @Override
    @GetMapping("/korean/live-price/{code}")
    public ResponseEntity<ApiResponse<KoreanLiveStockPriceResponse>> getKoreanStockLivePrice(@PathVariable String code){
        KoreanLiveStockPriceResponse koreanLiveStockPriceResponse = stockService.getLiveStockPrice(code);
        return ResponseEntity.ok(ApiResponse.ok(koreanLiveStockPriceResponse));
    }

}
