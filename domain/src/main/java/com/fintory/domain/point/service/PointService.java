package com.fintory.domain.point.service;

import com.fintory.domain.child.model.Child;
import com.fintory.domain.point.dto.PointWalletResponse;

public interface PointService {

    void givePointsByContinuousDays(int continuousDays, Child child);

    void createInitialPointWallet(Child child);

    PointWalletResponse getPointWalletWithTransactions(Child child);

    int getTotalAmount(Child child);
}
