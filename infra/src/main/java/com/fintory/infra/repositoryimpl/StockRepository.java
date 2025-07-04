package com.fintory.infra.repositoryimpl;

import com.fintory.domain.stock.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockRepository extends JpaRepository<Stock, Long> {
    @Query("SELECT s.code FROM Stock s WHERE s.code IN :codeList")
    List<String> findByInCodeList(@Param("codeList") List<String> koreanStockMarketCapTop20);
}
