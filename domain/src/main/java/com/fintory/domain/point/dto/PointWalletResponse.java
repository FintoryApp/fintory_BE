package com.fintory.domain.point.dto;

import com.fintory.domain.point.model.PointWallet;

import java.util.List;

public record PointWalletResponse(

        int totalAmount,
        List<PointTransactionsResponse> transactions

) {
    public static PointWalletResponse from(PointWallet wallet) {
        return new PointWalletResponse(
                wallet.getTotalAmount(),
                wallet.getTransactions().stream()
                        .map(PointTransactionsResponse::from)
                        .toList()
        );
    }
}
