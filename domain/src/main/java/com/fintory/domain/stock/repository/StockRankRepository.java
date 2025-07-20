package com.fintory.domain.stock.repository;

import com.fintory.domain.stock.model.StockRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRankRepository extends JpaRepository<StockRank, Long> {

    @Query("SELECT sr FROM StockRank sr WHERE sr.marketCapRank BETWEEN 1 AND 20 ORDER BY sr.marketCapRank ASC")
    List<StockRank> findMarketCapTop20();

    @Query("SELECT sr FROM StockRank sr WHERE sr.rocRank BETWEEN 1 AND 20 ORDER BY sr.rocRank ASC")
    List<StockRank> findROCTop20();

    @Query("SELECT sr FROM StockRank sr WHERE sr.tradingVolumeRank BETWEEN 1 AND 20 ORDER BY sr.tradingVolumeRank ASC")
    List<StockRank> findTradingVolumeTop20();


}
