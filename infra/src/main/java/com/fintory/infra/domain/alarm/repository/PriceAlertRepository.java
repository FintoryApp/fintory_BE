package com.fintory.infra.domain.alarm.repository;

import com.fintory.domain.alarm.model.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceAlertRepository extends JpaRepository<PriceAlert,Long> {

    List<PriceAlert> findByChildIdAndStockCode(Long id, String stockCode);
}
