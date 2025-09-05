package com.fintory.infra.domain.portfolio.repository;

import com.fintory.domain.portfolio.model.StockTransaction;
import com.fintory.domain.stock.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction,Long> {

    List<StockTransaction> findByStockOrderByExecutedAt(Stock stock);
}
