package com.fintory.child.stock.controller;

import com.fintory.child.stock.dto.*;
import com.fintory.child.stock.service.CommonStockServiceImpl;
import com.fintory.child.stock.service.KoreanStockServiceImpl;
import com.fintory.child.stock.service.OverseasStockServiceImpl;
import com.fintory.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stock")
@Slf4j
public class StockController {

    private final KoreanStockServiceImpl stockService;
    private final OverseasStockServiceImpl overseasStockService;
    private final CommonStockServiceImpl  commonStockService;



    // 국내 주식 현재가 데이터
    @GetMapping("/korean/realprice/{stockCode}")
    public ApiResponse<KoreanStockRealPrice> getKoreanStockRealPrice(@PathVariable String stockCode) {
        return ApiResponse.ok(stockService.getKoreanStockRealPrice(stockCode));
    }


    // 국내 주식 호가 데이터
    @GetMapping("/korean/pricequote/{stockCode}")
    public ApiResponse<KoreanPriceQuote> getPriceQuote(@PathVariable String stockCode) {
        return ApiResponse.ok(stockService.getPriceQuote(stockCode));
    }


    //주식 종목 검색
    @GetMapping("/search")
    public ApiResponse<List<KoreanSearchStock>> searchStock(@RequestParam String keyword) {


        List<KoreanSearchStock> koreanSearchStocks = commonStockService.searchStock(keyword);
        return ApiResponse.ok(koreanSearchStocks);
    }

    //국내 주식 랭킹 - 시가 총액
    @GetMapping("korean/rankings/marketCap")
    public ApiResponse<List<KoreanStockMarketCapTop20>> getKoreanStockMarketCapTop20(){
        stockService.clearRealPriceWebSocketSubscriptions();
        List<KoreanStockMarketCapTop20> koreanStockMarketCapTop20s = stockService.getKoreanMarketCapTop20();
        return ApiResponse.ok(koreanStockMarketCapTop20s);
    }


    //국내 주식 랭킹 - 등락률
    @GetMapping("korean/rankings/roc")
    public ApiResponse<List<KoreanStockROCTop20>> getKoreanStockROCTop20(){
        stockService.clearRealPriceWebSocketSubscriptions();
        List<KoreanStockROCTop20> koreanStockROCTop20s = stockService.getKoreanROCTop20();
        return ApiResponse.ok(koreanStockROCTop20s);
    }

    //국내 주식 랭킹 - 거래량
    @GetMapping("korean/rankings/tradingVolume")
    public ApiResponse<List<KoreanStockTradingVolume>> getKoreanStockTradingVolumeTop20(){
        List<KoreanStockTradingVolume> koreanStockTradingVolumes = stockService.getKoreanTradingVolumeTop20();
        return ApiResponse.ok(koreanStockTradingVolumes);
    }

    //국내 주식 차트 데이터
    @GetMapping("/korean/stockInfo/{code}")
    public ApiResponse<KoreanStockDetailInfo> getKoreanStockDetailInfo(@PathVariable String code){
        Map<String,List<KoreanStockChart>> chartData = new HashMap<>();

        KoreanStockInfo stockInfo = stockService.getKoreanStockInfo(code);

        chartData.put("1D", stockService.getKoreanStockItemChatPriceDay(code));
        chartData.put("1W", stockService.getKoreanStockItemChatPriceWeek(code));
        chartData.put("3M", stockService.getKoreanStockItemChatPrice3Month(code));
        chartData.put("1Y", stockService.getKoreanStockItemChatPriceYear(code));
        chartData.put("5Y", stockService.getKoreanStockItemChatPrice5Year(code));
        chartData.put("total", stockService.getKoreanStockItemChatPriceTotal(code));

        KoreanStockDetailInfo koreanStockDetailInfo = KoreanStockDetailInfo.builder()
                .name(stockInfo.getName())
                .code(code)
                .chartData(chartData)
                .build();

        return ApiResponse.ok(koreanStockDetailInfo);
    }



    //해외 주식 현재가 데이터
    @GetMapping("/overseas/realprice/{stockCode}")
    public ApiResponse<OverseasStockRealPrice> getOverseasStockRealPrice(@PathVariable String stockCode) {
        return ApiResponse.ok(overseasStockService.getOverseasStockRealPrice(stockCode));
    }

    //해외 주식 랭킹 - 시가 총액
    @GetMapping("overseas/rankings/marketCap")
    public ApiResponse<List<OverseasStockMarketCapTop20>> getOverseasStockMarketCapTop20(){
        List<OverseasStockMarketCapTop20> overseasStockMarketCapTop20s = overseasStockService.getOverseasStockMarketCapTop20();
        return ApiResponse.ok(overseasStockMarketCapTop20s);
    }


    //해외 주식 랭킹 - 등락률
    @GetMapping("overseas/rankings/roc")
    public ApiResponse<List<OverseasStockROCTop20>> getOverseasStockROCTop20(){
        List<OverseasStockROCTop20> overseasStockROCTop20s = overseasStockService.getOverseasStockROCTop20();
        return ApiResponse.ok(overseasStockROCTop20s);
    }


    //해외 주식 랭킹 - 거래량
    @GetMapping("overseas/rankings/tradingVolume")
    public ApiResponse<List<OverseasStockTradingVolume>> getOverseasStockTradingVolumeTop20(){
        List<OverseasStockTradingVolume> overseasStockTradingVolumes = overseasStockService.getOverseasStockTradingVolumeTop20();
        return ApiResponse.ok(overseasStockTradingVolumes);
    }

    //해외 주식 - 차트 데이터
    @GetMapping("/overseas/stockInfo/{code}")
    public ApiResponse<OverseasStockDetailInfo> getOverseasStockDetailInfo(@PathVariable String code){
        Map<String,List<OverseasStockChart>> chartData = new HashMap<>();

        OverseasStockInfo stockInfo = overseasStockService.getOverseasStockInfo(code);

        chartData.put("1D", overseasStockService.getOverseasStockItemChatPriceDay(code));
        chartData.put("1W", overseasStockService.getOverseasStockItemChatPriceWeek(code));
        chartData.put("3M", overseasStockService.getOverseasStockItemChatPrice3Month(code));
        chartData.put("1Y", overseasStockService.getOverseasStockItemChatPriceYear(code));
        chartData.put("5Y", overseasStockService.getOverseasStockItemChatPrice5Year(code));
        chartData.put("total", overseasStockService.getOverseasStockItemChatPriceTotal(code));

        OverseasStockDetailInfo overseasStockDetailInfo = OverseasStockDetailInfo.builder()
                .engName(stockInfo.getEngName())
                .name(stockInfo.getName())
                .code(code)
                .chartData(chartData)
                .build();

        return ApiResponse.ok(overseasStockDetailInfo);
    }




}
