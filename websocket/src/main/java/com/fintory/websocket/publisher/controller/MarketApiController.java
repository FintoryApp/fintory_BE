package com.fintory.websocket.publisher.controller;

import com.fintory.domain.stock.dto.websocket.MarketStatusResponse;
import com.fintory.websocket.publisher.service.MarketTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/websocket/market")
@RequiredArgsConstructor
public class MarketApiController {
    private final MarketTimeService marketTimeService;

    @GetMapping("/status")
    public MarketStatusResponse getMarketStatus() {
        return marketTimeService.getMarketStatus();
    }
}
