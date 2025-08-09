package com.fintory.child.domain.stock.controller.common;

import com.fintory.common.api.ApiResponse;
import com.fintory.domain.stock.dto.korean.response.StockSearchResponse;
import com.fintory.domain.stock.service.CommonStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommonStockControllerImpl implements CommonStockController {

    private final CommonStockService commonStockService;

    //주식 종목 검색
    @Override
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<StockSearchResponse>>> searchStock(@RequestParam String keyword) {
        List<StockSearchResponse> stockSearchRespons = commonStockService.searchStock(keyword);
        return ResponseEntity.ok(ApiResponse.ok(stockSearchRespons));
    }

}
