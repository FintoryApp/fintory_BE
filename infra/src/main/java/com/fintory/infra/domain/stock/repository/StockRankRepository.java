package com.fintory.infra.domain.stock.repository;

import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.model.StockRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockRankRepository extends JpaRepository<StockRank,Long> {
    @Query("SELECT sr FROM StockRank sr JOIN FETCH sr.stock")
    List<StockRank> findAllStockRanks();

    @Query("SELECT sr FROM StockRank sr JOIN FETCH sr.stock WHERE sr.stock = :stock")
    Optional<StockRank> findByStock(@Param("stock") Stock stock);

    @Query("SELECT sr FROM StockRank sr JOIN FETCH sr.stock WHERE sr.stock.currencyName = :currencyName ORDER BY sr.marketCapRank ASC")
    List<StockRank> findMarketCapTop20(@Param("currencyName") String currencyName);

    @Query("SELECT sr FROM StockRank sr JOIN FETCH sr.stock WHERE sr.stock.currencyName = :currencyName ORDER BY sr.rocRank ASC")
    List<StockRank> findROCTop20(@Param("currencyName") String currencyName);

    @Query("SELECT sr FROM StockRank sr JOIN FETCH sr.stock WHERE sr.stock.currencyName = :currencyName ORDER BY sr.tradingVolumeRank ASC")
    List<StockRank> findTradingVolumeTop20(@Param("currencyName") String currencyName);

    @Query("SELECT sr FROM StockRank sr JOIN FETCH sr.stock WHERE sr.stock.currencyName = :currencyName")
    List<StockRank> findByCurrencyName(@Param("currencyName") String currencyName);
}
