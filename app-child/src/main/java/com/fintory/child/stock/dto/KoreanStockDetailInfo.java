package com.fintory.child.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KoreanStockDetailInfo {

    private String code;
    private String name;
    private Map<String, List<KoreanStockChart>> chartData;
}
