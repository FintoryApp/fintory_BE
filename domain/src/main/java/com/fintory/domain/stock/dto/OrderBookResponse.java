package com.fintory.domain.stock.dto;


import com.fintory.domain.stock.model.OrderBook;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public record OrderBookResponse(

        List<PriceLevel> sellLevels,
        List<PriceLevel> buyLevels,
        String stockCode,
        String stockName
){
    public record PriceLevel(
            BigDecimal price,
            Long quantity
    ){}

    public static OrderBookResponse convertFromOrderBook(OrderBook orderBook){
        List<PriceLevel> sellLevels = new ArrayList<>();
        List<PriceLevel> buyLevels = new ArrayList<>();
        try {
            for (int i = 1; i <= 10; i++) {
                Method sellPriceMethod = OrderBook.class.getMethod("getSellPrice" + i);
                Method sellQuantityMethod = OrderBook.class.getMethod("getSellQuantity" + i);
                BigDecimal sellPrice = (BigDecimal) sellPriceMethod.invoke(orderBook);
                Long sellQuantity = (Long) sellQuantityMethod.invoke(orderBook);
                sellLevels.add(new PriceLevel(sellPrice, sellQuantity));

                Method buyPriceMethod = OrderBook.class.getMethod("getBuyPrice" + i);
                Method buyQuantityMethod = OrderBook.class.getMethod("getBuyQuantity" + i);
                BigDecimal buyPrice = (BigDecimal) buyPriceMethod.invoke(orderBook);
                Long buyQuantity = (Long) buyQuantityMethod.invoke(orderBook);
                buyLevels.add(new PriceLevel(buyPrice, buyQuantity));
            }
        }catch (Exception e){
            log.error("OrderBook dto 변환하는 과정에서 에러 발생:{}",e.getMessage());
        }

        return new OrderBookResponse(
                sellLevels,
                buyLevels,
                orderBook.getStock().getCode(),
                orderBook.getStock().getName()
        );
    }
}
