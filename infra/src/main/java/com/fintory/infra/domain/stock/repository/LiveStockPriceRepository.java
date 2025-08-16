package com.fintory.infra.domain.stock.repository;

import com.fintory.domain.stock.model.LiveStockPrice;
import com.fintory.domain.stock.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LiveStockPriceRepository extends JpaRepository<LiveStockPrice,Long> {

    @Query("SELECT lsp FROM LiveStockPrice lsp JOIN FETCH lsp.stock WHERE lsp.stock=:stock")
    Optional<LiveStockPrice> findByStock(@Param("stock") Stock stock);
}
