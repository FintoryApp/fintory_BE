package com.fintory.websocket.publisher.service;

import com.fintory.domain.stock.dto.websocket.LiveStockPriceStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
@RequiredArgsConstructor
public class StockDataBatchSaveService {

    private final LiveStockPriceWebSocketSaverService liveStockPriceWebSocketSaverService;

    public void saveBatchData(String marketName, Map<String, LiveStockPriceStream> pendingData) {
        if (pendingData.isEmpty()) return;

        Map<String, LiveStockPriceStream> dataToSave = new HashMap<>(pendingData);
        pendingData.clear();

        dataToSave.values().forEach(dto -> {
            try {
                liveStockPriceWebSocketSaverService.saveStockData(dto);
            } catch (Exception e) {
                log.error("{} 종목 {} 저장 실패: {}", marketName, dto.code(), e.getMessage());
            }
        });

        log.info("{} 주식 배치 저장 완료 - 저장된 종목 수: {}", marketName, dataToSave.size());
    }

    public void saveRemainingData(String marketName, Map<String, LiveStockPriceStream> pendingData) {
        if (!pendingData.isEmpty()) {

            Map<String, LiveStockPriceStream> dataToSave = new HashMap<>(pendingData);
            pendingData.clear();

            dataToSave.values().forEach(dto -> {
                try {
                    liveStockPriceWebSocketSaverService.saveStockData(dto);
                } catch (Exception e) {
                    log.error("{} 종목 {} 마지막 저장 실패: {}", marketName, dto.code(), e.getMessage());
                }
            });

            log.info("{} 주식 마지막 배치 저장 완료 - 저장된 종목 수: {}", marketName, dataToSave.size());
        }
    }

}
