package com.fintory.domain.stock.dto;

import com.fintory.domain.stock.model.IntervalType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record StockPriceHistoryWrapper(
        String code,
        String name,
        Map<IntervalType, List<StockPriceHistoryResponse>> charData
) {
    public StockPriceHistoryWrapper(String code, String name){
        this(code, name, createInitialMap());
    }

    private static Map<IntervalType, List<StockPriceHistoryResponse>> createInitialMap() {
        Map<IntervalType, List<StockPriceHistoryResponse>> map = new HashMap<>();
        map.put(IntervalType.FIVEYEARLY, null);
        map.put(IntervalType.MONTHLY, null);
        map.put(IntervalType.YEARLY, null);
        map.put(IntervalType.WEEKLY, null);
        map.put(IntervalType.TOTAL, null);
        return map;
    }
}
