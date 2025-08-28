package com.fintory.infra.domain.stock.repository;

import com.fintory.domain.stock.model.IntervalType;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.model.StockPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockPriceHistoryRepository extends JpaRepository<StockPriceHistory,Long> {

    List<StockPriceHistory> findByStockAndIntervalType(Stock stock, IntervalType intervalType);

    void deleteByStockAndIntervalType(Stock stock, IntervalType intervalType);
}
