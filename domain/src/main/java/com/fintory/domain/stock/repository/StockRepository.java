package com.fintory.domain.stock.repository;

import com.fintory.domain.stock.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    @Query("SELECT s.code FROM Stock s WHERE s.code IN :codes")
    List<String> findByCodeList(@Param("codes") List<String> codeList);

    Optional<Stock> findByCode(String code);

    Optional<Stock> findByName(String stockName);

    @Query("SELECT s FROM Stock s WHERE s.name LIKE CONCAT('%',:keyword,'%') ")
    List<Stock> findByNameContaining(String keyword);

    @Query("SELECT s FROM Stock s WHERE s.code LIKE CONCAT('%',:keyword,'%') ")
    List<Stock> findByCodeContaining(String keyword);
}
