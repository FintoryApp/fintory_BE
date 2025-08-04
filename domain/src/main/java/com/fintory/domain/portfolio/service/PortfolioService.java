package com.fintory.domain.portfolio.service;

import com.fintory.domain.portfolio.dto.OwnedStockMetrics;
import com.fintory.domain.portfolio.dto.PortfolioSummary;
import java.util.List;

/**
 * 포트폴리오 관련 비즈니스 로직을 정의하는 서비스 인터페이스
 *
 *
 * @author Mh
 * @since 2025-08-05
 */
public interface PortfolioService {
    /**
     * 사용자의 보유 주식 목록 조회
     *
     * @return 보유 주식 지표(평균매수가, 총 수량) 및 거래 내역 리스트
     * @throws DomainException 계정을 찾을 수 없거나 조회 중 에러 발생시
     */
    public List<OwnedStockMetrics> getOwnedStockMetrics();

    /**
     *
     * 주식별 현재 포트폴리오 요약 조회
     *
     * @return 포트폴리오 요약 정보 (총 매수금액, 총 자산)
     * @throws DomainException 계정을 찾을 수 없거나 계산 중 에러 발생시
     */
    public PortfolioSummary getPortfolioSummary();
}
