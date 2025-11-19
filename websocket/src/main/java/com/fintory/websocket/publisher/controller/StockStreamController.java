package com.fintory.websocket.publisher.controller;

import com.fintory.domain.stock.dto.websocket.LiveStockPriceStream;
import com.fintory.websocket.monitoring.config.SSEMetrics;
import com.fintory.websocket.publisher.handler.StockStreamBridge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
@Slf4j
public class StockStreamController {

    private final StockStreamBridge stockStreamBridge;
    private final SSEMetrics sseMetrics;

    @GetMapping(value="/live-price",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<LiveStockPriceStream>> streamAll(){
        return stockStreamBridge.getStream()
                .map(data-> ServerSentEvent.<LiveStockPriceStream>builder()
                        .data(data)
                        .build())
                .onBackpressureLatest()
                .doOnSubscribe(sub->{
                    sseMetrics.incrementConnection();
                    sseMetrics.incrementSubscriber();
                })
                .doOnNext(data->{
                    sseMetrics.incrementMessageSent();
                })
                .doOnCancel(()->{
                    sseMetrics.decrementConnection();
                    sseMetrics.decrementSubscriber();
                })
                .doOnComplete(()->{
                    sseMetrics.decrementSubscriber();
                    sseMetrics.decrementConnection();
                })
                .doOnError(error->{
                    sseMetrics.decrementConnection();
                    sseMetrics.decrementSubscriber();
                })
                .doOnDiscard(LiveStockPriceStream.class, discarded->{
                    sseMetrics.incrementMessageDropped();
                });
    }
}
