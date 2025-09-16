package com.fintory.infra.domain.stock.repository;

import com.fintory.domain.stock.model.IntervalType;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.model.StockPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockPriceHistoryRepository extends JpaRepository<StockPriceHistory,Long> {

    List<StockPriceHistory> findByStockAndIntervalType(Stock stock, IntervalType intervalType);

    void deleteByStockAndIntervalType(Stock stock, IntervalType intervalType);

    @Query("SELECT sph.closePrice FROM StockPriceHistory sph WHERE sph.stock =:stock ORDER BY sph.date DESC LIMIT 1 ")
    BigDecimal findByStockAndDate(Stock stock);

}
