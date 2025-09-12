package com.fintory.domain.point.service;

import com.fintory.domain.child.model.Child;

public interface PointService {

    void givePointsByContinuousDays(int continuousDays, Child child);

    void createInitialPointWallet(Child child);

    // TODO:List<PointTransaction> getAllPointTransactions(Child child);
}
