package com.fintory.domain.stock.service.websocket;

import com.fintory.domain.stock.dto.websocket.LiveStockPriceStream;

public interface LiveStockPriceWebSocketSaverService {
     void saveStockData(LiveStockPriceStream dto);
}
