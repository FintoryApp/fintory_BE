package com.fintory.domain.account.service;

import com.fintory.domain.account.dto.response.DepositTransactionResponse;
import com.fintory.domain.child.model.Child;

import java.util.List;

public interface AccountService {

    void createInitialAccount(Child child);

    List<DepositTransactionResponse> getDepositTransactionsByChild(Child child);
}
