package com.fintory.domain.stock.dto.overseas.response;

import com.fintory.domain.stock.dto.korean.core.KoreanStockPriceHistory;
import com.fintory.domain.stock.dto.overseas.core.OverseasStockPriceHistory;

import java.util.List;
import java.util.Map;

public record OverseasStockPriceHistoryResponse(
        String code,
        String name,
        Map<String, List<OverseasStockPriceHistory>> chartData
){}

