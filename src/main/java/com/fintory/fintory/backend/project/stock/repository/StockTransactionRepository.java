package com.fintory.fintory.backend.project.stock.repository;

import com.fintory.fintory.backend.project.stock.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

}
