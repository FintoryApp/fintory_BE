package com.fintory.domain.portfolio.repository;

import com.fintory.domain.portfolio.model.OwnedStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OwnedStockRepository extends JpaRepository<OwnedStock,Long> {
}
