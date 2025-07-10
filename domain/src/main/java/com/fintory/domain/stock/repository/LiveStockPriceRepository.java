package com.fintory.domain.stock.repository;

import com.fintory.domain.stock.model.LiveStockPrice;
import com.fintory.domain.stock.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LiveStockPriceRepository extends JpaRepository<LiveStockPrice,Long> {
    LiveStockPrice findByStock(Stock stock);
}
