package com.fintory.domain.portfolio.dto.summary;

import com.fintory.domain.portfolio.model.OwnedStock;
import com.fintory.domain.stock.model.Stock;

import java.math.BigDecimal;

public record OwnedStockDetails(

        String stockCode,
        String stockName,
        String profileImageUrl,
        BigDecimal currentPrice,
        BigDecimal quantity,
        BigDecimal purchaseamount,
        BigDecimal averagePurchasePrice
) {
    public static OwnedStockDetails from(OwnedStock ownedStock,BigDecimal currentPrice) {
        Stock stock = ownedStock.getStock();

        return new OwnedStockDetails(
                stock.getCode(),
                stock.getName(),
                stock.getCompanyImageUrl(),
                currentPrice,
                ownedStock.getQuantity(),
                ownedStock.getPurchaseAmount(),
                ownedStock.getAveragePurchasePrice()
        );
    }
}
