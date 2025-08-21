package com.fintory.infra.domain.stock.repository;

import com.fintory.domain.stock.model.OrderBook;
import com.fintory.domain.stock.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderBookRepository extends JpaRepository<OrderBook,Long> {
    @Query("SELECT ob FROM OrderBook ob JOIN FETCH ob.stock WHERE ob.stock=:stock")
    Optional<OrderBook> findByStock(@Param("stock") Stock stock);
}
