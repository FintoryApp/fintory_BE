package com.fintory.infra.domain.portfolio.repository;

import com.fintory.domain.account.model.Account;
import com.fintory.domain.portfolio.model.OwnedStock;
import com.fintory.domain.stock.model.Stock;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OwnedStockRepository extends JpaRepository<OwnedStock,Long> {
    List<OwnedStock> findByAccount(Account account);

    @Query("SELECT os FROM OwnedStock os JOIN FETCH os.stock WHERE os.account = :account")
    List<OwnedStock> findAllWithStockByAccount(@Param("account") Account account);

    Optional<OwnedStock> findByAccountAndStock(Account account, Stock stock);

    List<OwnedStock> findAllWithStockByAccountAndStock_CurrencyName(Account account, String currencyName);
}
