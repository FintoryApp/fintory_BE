package com.fintory.domain.account.dto.response;

import com.fintory.domain.account.model.Account;

import java.math.BigDecimal;

public record TotalAssetsResponse(
        BigDecimal totalAssets
) {
    public static TotalAssetsResponse from(Account account) {
        return new TotalAssetsResponse(account.getTotalAssets());
    }
}
