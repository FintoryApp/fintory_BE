package com.fintory.domain.portfolio.dto.summary;

import com.fintory.domain.portfolio.model.OwnedStock;
import com.fintory.domain.stock.model.Stock;

import java.math.BigDecimal;

public record OwnedStockDetails(

        String stockCode,
        String stockName,
        BigDecimal currentPrice,
        BigDecimal quantity,
        BigDecimal purchaseamount,
        BigDecimal averagePurchase
) {
    public static OwnedStockDetails from(OwnedStock ownedStock,BigDecimal currentPrice) {
        Stock stock = ownedStock.getStock();

        return new OwnedStockDetails(
                stock.getCode(),
                stock.getName(),
                currentPrice,
                ownedStock.getQuantity(),
                ownedStock.getPurchaseAmount(),
                ownedStock.getAveragePurchasePrice()
        );
    }
}
