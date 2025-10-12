package com.fintory.domain.portfolio.dto.summary;

import com.fintory.domain.portfolio.model.OwnedStock;
import com.fintory.domain.stock.model.Stock;

import java.math.BigDecimal;

public record OwnedStockDetails(

        String stockCode,
        String stockName,

        BigDecimal quantity,
        BigDecimal purchaseamount
) {
    public static OwnedStockDetails from(OwnedStock ownedStock) {
        Stock stock = ownedStock.getStock();

        return new OwnedStockDetails(
                stock.getCode(),
                stock.getName(),
                ownedStock.getQuantity(),
                ownedStock.getPurchaseAmount()
        );
    }
}
