package com.fintory.infra.domain.account.repository;

import com.fintory.domain.account.model.Account;
import com.fintory.domain.account.model.DepositTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepositTransactionRepository extends JpaRepository<DepositTransaction, Long> {

    List<DepositTransaction> findByAccountOrderByOccurredAtDesc(Account account);
}
