package com.fintory.infra.domain.stock.repository;

import com.fintory.domain.stock.model.IntervalType;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.model.StockPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockPriceHistoryRepository extends JpaRepository<StockPriceHistory,Long> {
    void deleteByStock(Stock stock);

    void deleteByStockAndIntervalType(Stock stock, IntervalType intervalType);

    @Query("SELECT sph FROM StockPriceHistory sph JOIN FETCH sph.stock WHERE sph.stock=:stock")
    List<StockPriceHistory> findByStock(@Param("stock") Stock stock);
}
