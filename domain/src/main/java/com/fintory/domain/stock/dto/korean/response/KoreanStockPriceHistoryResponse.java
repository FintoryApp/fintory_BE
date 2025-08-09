package com.fintory.domain.stock.dto.korean.response;

import com.fintory.domain.stock.dto.korean.core.KoreanStockPriceHistory;
import java.util.List;
import java.util.Map;


public record KoreanStockPriceHistoryResponse(
     String code,
     String name,
     Map<String, List<KoreanStockPriceHistory>> chartData
){}
