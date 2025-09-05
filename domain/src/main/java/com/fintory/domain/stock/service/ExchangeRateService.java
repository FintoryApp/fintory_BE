package com.fintory.domain.stock.service;

import java.math.BigDecimal;

public interface ExchangeRateService {

    /**
     * ECOS 에서 제공하는 오늘의 환율 값 조회 메소드
     * @return 환율 값
     */
    BigDecimal getExchangeRate();
}
