package com.fintory.infra.domain.stock.repository;

import com.fintory.domain.stock.model.StockRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRankRepository extends JpaRepository<StockRank, Long> {
    Optional<StockRank> findByStockCode(String code);

    @Query("SELECT sr, lsp FROM StockRank sr JOIN FETCH sr.stock s JOIN LiveStockPrice lsp ON lsp.stock =s WHERE  sr.stock.currencyName=:currencyName ORDER BY sr.marketCapRank ASC")
    List<Object[]> findMarketCapTop20(String currencyName);

    @Query("SELECT sr, lsp FROM StockRank sr JOIN FETCH sr.stock s JOIN LiveStockPrice lsp ON lsp.stock = s WHERE sr.stock.currencyName=:currencyName ORDER BY sr.rocRank ASC")
    List<Object[]> findROCTop20(String currencyName);

    @Query("SELECT sr, lsp FROM StockRank sr JOIN FETCH sr.stock s JOIN LiveStockPrice lsp ON lsp.stock = s WHERE sr.stock.currencyName=:currencyName ORDER BY sr.tradingVolumeRank ASC")
    List<Object[]> findTradingVolumeTop20(String currencyName);

    @Query("SELECT sr FROM StockRank sr WHERE sr.stock.currencyName=:currencyName")
    List<StockRank> findByCurrencyName(String currencyName);
}
