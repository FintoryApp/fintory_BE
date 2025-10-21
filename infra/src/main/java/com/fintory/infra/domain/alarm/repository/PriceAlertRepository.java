package com.fintory.infra.domain.alarm.repository;

import com.fintory.domain.alarm.model.PriceAlert;
import com.fintory.domain.child.model.Child;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface PriceAlertRepository extends JpaRepository<PriceAlert,Long> {

    List<PriceAlert> findByChildIdAndStockCode(Long id, String stockCode);
    boolean existsByChildIdAndStockCodeAndTargetPrice(Long id, String code, BigDecimal bigDecimal);

    List<PriceAlert> findByChildAndTargetPrice(Child child, BigDecimal currentPrice);

    List<PriceAlert> findByStockCode(String code);
}
