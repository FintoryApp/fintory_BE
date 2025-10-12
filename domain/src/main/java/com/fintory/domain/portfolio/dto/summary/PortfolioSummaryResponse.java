package com.fintory.domain.portfolio.dto.summary;

import com.fintory.domain.account.model.Account;
import com.fintory.domain.portfolio.model.OwnedStock;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public record PortfolioSummaryResponse(

        BigDecimal availableCash,
        List<OwnedStockDetails>  ownedStockDetails
) {

    public static PortfolioSummaryResponse from(
            Account account,
            List<OwnedStock> ownedStocks
    ) {
        List<OwnedStockDetails> stockDetails = ownedStocks.stream()
                .map(OwnedStockDetails::from)
                .collect(Collectors.toList());

        return new PortfolioSummaryResponse(
                account.getAvailableCash(),
                stockDetails
        );
    }
}
