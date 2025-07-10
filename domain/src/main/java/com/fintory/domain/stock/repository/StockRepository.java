package com.fintory.domain.stock.repository;

import com.fintory.domain.stock.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    @Query("SELECT s.code FROM Stock s WHERE s.code IN :codeList")
    List<String> findByInCodeList(@Param("codeList") List<String> koreanStockMarketCapTop20);

    Stock findByCode(String code);
}
