package com.fintory.websocket.publisher.repository;

import com.fintory.domain.stock.model.IntervalType;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.model.StockPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockPriceHistoryRepository extends JpaRepository<StockPriceHistory,Long> {


    @Query("SELECT sph FROM StockPriceHistory sph WHERE sph.stock=:stock AND sph.intervalType=:intervalType AND sph.date=:now ORDER BY sph.updatedAt ASC LIMIT 1")
    Optional<StockPriceHistory> findOldestByStockAndIntervalTypeAndDate(Stock stock, IntervalType intervalType, LocalDate now);

    void deleteByStockAndIntervalTypeAndDateBefore(Stock stock, IntervalType intervalType, LocalDate now);

    List<StockPriceHistory> findByStockAndIntervalTypeAndDate(Stock stock, IntervalType intervalType, LocalDate localDate);
}
