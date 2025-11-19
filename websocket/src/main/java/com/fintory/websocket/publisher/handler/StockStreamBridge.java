package com.fintory.websocket.publisher.handler;

import com.fintory.domain.stock.dto.websocket.LiveStockPriceStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
@Slf4j
public class StockStreamBridge {

    private final Sinks.Many<LiveStockPriceStream> sink;

    public StockStreamBridge() {
        this.sink = Sinks.many()
                .multicast()
                .directBestEffort();
    }

    public Flux<LiveStockPriceStream> getStream(){
        return sink.asFlux();
    }

    public void publish(LiveStockPriceStream data){
        sink.tryEmitNext(data);
    }
}
