package com.fintory.infra.domain.portfolio.repository;

import com.fintory.domain.portfolio.model.StockTransaction;
import com.fintory.domain.stock.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction,Long> {

    List<StockTransaction> findByStockOrderByExecutedAt(Stock stock);


    @Query("SELECT st FROM StockTransaction st JOIN FETCH st.account a JOIN FETCH a.child WHERE st.executedAt BETWEEN :startTime AND :endTime AND a.child = :child")
    List<StockTransaction> findByExecutedAtBetweenAndAccount_ChildWithFetch(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("child") Object child
    );

}
