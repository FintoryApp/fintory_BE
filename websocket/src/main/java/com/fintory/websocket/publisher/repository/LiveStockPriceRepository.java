package com.fintory.websocket.publisher.repository;

import com.fintory.domain.stock.model.LiveStockPrice;
import com.fintory.domain.stock.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LiveStockPriceRepository extends JpaRepository<LiveStockPrice,Long> {
    Optional<LiveStockPrice> findByStock(Stock stock);
}
