package com.fintory.domain.portfolio.repository;

import com.fintory.domain.portfolio.model.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction,Long> {
    List<StockTransaction> findByExecutedAtAfter(LocalDateTime localDateTime);
}
