package com.fintory.infra.domain.stock.repository;

import com.fintory.domain.stock.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock,Long> {
    Optional<Stock> findByCode(String code);
    List<Stock> findByCurrencyName(String krw);

    @Query("SELECT s FROM Stock s Where s.name LIKE CONCAT('%',:keyword,'%') OR s.code LIKE CONCAT('%',:keyword,'%') OR s.eng_name LIKE CONCAT('%',:keyword,'%')")
    List<Stock> findByNameContainingOrCodeContaining(@Param("keyword") String keyword);
}
