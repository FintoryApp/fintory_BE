package com.fintory.domain.account.model;

import lombok.Getter;

@Getter
public enum DepositTransactionType {

    WITHDRAW("출금"),
    DEPOSIT("입금");

    private final String type;

    DepositTransactionType(String type) {
        this.type = type;
    }
}
