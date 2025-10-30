package com.fintory.infra.domain.stock.repository;

import com.fintory.domain.stock.model.IntervalType;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.model.StockPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockPriceHistoryRepository extends JpaRepository<StockPriceHistory,Long> {

    List<StockPriceHistory> findByStockAndIntervalType(Stock stock, IntervalType intervalType);

    void deleteByStockAndIntervalType(Stock stock, IntervalType intervalType);

    @Query("SELECT sph.closePrice FROM StockPriceHistory sph WHERE sph.stock =:stock ORDER BY sph.date DESC LIMIT 1 ")
    BigDecimal findByStockAndDate(Stock stock);

    @Query("SELECT sph FROM StockPriceHistory sph WHERE sph.stock=:stock AND sph.intervalType=:intervalType AND sph.date=:now ORDER BY sph.updatedAt ASC LIMIT 1")
    Optional<StockPriceHistory> findOldestByStockAndIntervalTypeAndDate(Stock stock, IntervalType intervalType, LocalDate now);

    List<StockPriceHistory> findByStockAndIntervalTypeOrderByDateAsc(Stock stock, IntervalType intervalType);

    void deleteByStockAndIntervalTypeAndDateBefore(Stock stock, IntervalType intervalType, LocalDate now);

    List<StockPriceHistory> findByStockAndIntervalTypeOrderByUpdatedAtAsc(Stock stock, IntervalType intervalType);


    Optional<StockPriceHistory> findFirstByStockAndIntervalTypeOrderByUpdatedAtDesc(Stock stock, IntervalType intervalType);

    List<StockPriceHistory> findByStockAndIntervalTypeAndDate(Stock stock, IntervalType intervalType, LocalDate localDate);
}
