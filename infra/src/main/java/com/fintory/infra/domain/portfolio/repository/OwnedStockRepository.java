package com.fintory.infra.domain.portfolio.repository;

import com.fintory.domain.account.model.Account;
import com.fintory.domain.portfolio.model.OwnedStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OwnedStockRepository extends JpaRepository<OwnedStock,Long> {
    List<OwnedStock> findByAccount(Account account);
}
