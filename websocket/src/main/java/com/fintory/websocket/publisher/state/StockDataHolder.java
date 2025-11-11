package com.fintory.websocket.publisher.state;

import com.fintory.domain.stock.dto.websocket.LiveStockPriceStream;


import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Data
public class StockDataHolder {

    private final Set<String> koreanSubscribedStocks = ConcurrentHashMap.newKeySet();
    private final Set<String> overseasSubscribedStocks = ConcurrentHashMap.newKeySet();

    // 이전에 받은 주식 데이터 저장 -> 중복 데이터 필터링용
    private final Map<String, LiveStockPriceStream> previousKoreanData = new ConcurrentHashMap<>();
    private final Map<String, LiveStockPriceStream> previousOverseasData = new ConcurrentHashMap<>();

    // db에 저장되지 않은 주식 데이터 임시 저장용
    private final Map<String, LiveStockPriceStream> koreanPendingData = new ConcurrentHashMap<>();
    private final Map<String, LiveStockPriceStream> overseasPendingData = new ConcurrentHashMap<>();

    private final AtomicBoolean isKoreanConnected = new AtomicBoolean(false);
    private final AtomicBoolean  isOverseasConnected = new AtomicBoolean(false);

    private String cachedAccessToken;


}
