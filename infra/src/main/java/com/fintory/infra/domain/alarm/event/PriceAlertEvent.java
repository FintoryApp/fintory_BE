package com.fintory.infra.domain.alarm.event;

import com.fintory.domain.stock.dto.websocket.LiveStockPriceStream;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PriceAlertEvent extends ApplicationEvent {

    private final LiveStockPriceStream stockPriceStream;


    public PriceAlertEvent(Object source, LiveStockPriceStream stockPriceStream) {
        super(source);
        this.stockPriceStream = stockPriceStream;
    }
}
