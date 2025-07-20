package com.fintory.domain.stock.repository;

import com.fintory.domain.stock.model.OrderBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderBookRepository extends JpaRepository<OrderBook,Long> {
    OrderBook findByStockId(Long id);
}
