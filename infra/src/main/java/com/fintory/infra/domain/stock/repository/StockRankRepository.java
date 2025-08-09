package com.fintory.infra.domain.stock.repository;

import com.fintory.domain.stock.model.StockRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRankRepository extends JpaRepository<StockRank, Long> {
    @Query("SELECT sr FROM StockRank sr WHERE sr.stock.code=:code")
    Optional<StockRank> findByStockCode(String code);

    @Query("SELECT sr FROM StockRank sr WHERE sr.stock.currencyName=:currencyName ORDER BY sr.marketCap DESC")
    List<StockRank> findAllOrderByMarketCap(String currencyName);

    @Query("SELECT sr FROM StockRank sr WHERE sr.stock.currencyName=:currencyName ORDER BY sr.rocRate DESC")
    List<StockRank> findAllOrderByRocRate(String currencyName);

    @Query("SELECT sr FROM StockRank sr WHERE sr.stock.currencyName=:currencyName ORDER BY ABS(sr.tradingVolume) DESC")
    List<StockRank> findAllOrderByTradingVolume(String currencyName);

    @Query("SELECT sr FROM StockRank sr WHERE  sr.stock.currencyName=:currencyName ORDER BY sr.marketCapRank ASC")
    List<StockRank> findMarketCapTop20(String currencyName);

    @Query("SELECT sr FROM StockRank sr WHERE sr.stock.currencyName=:currencyName ORDER BY sr.rocRank ASC")
    List<StockRank> findROCTop20(String currencyName);

    @Query("SELECT sr FROM StockRank sr WHERE sr.stock.currencyName=:currencyName ORDER BY sr.tradingVolumeRank ASC")
    List<StockRank> findTradingVolumeTop20(String currencyName);

}
