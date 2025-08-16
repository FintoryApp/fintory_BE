package com.fintory.infra.domain.stock.service;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.IntraDayResponse;
import com.fintory.domain.stock.model.IntervalType;
import com.fintory.domain.stock.model.Stock;
import com.fintory.infra.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockPriceHistoryScheduler {
    private final StockPriceHistoryServiceImpl intraDayService;
    private final StockRepository stockRepository;

    // 1. 매일 새벽 1시 - 단기 데이터 (weekly, monthly)
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void updateShortTermData() {
        log.info("단기 데이터 업데이트 시작 (weekly, monthly)");
        try {
            List<Stock> allStocks = stockRepository.findAll();
            for (Stock stock : allStocks) {
                List<IntraDayResponse> weeklyData = intraDayService.fetchIntraDayDataByWeek(stock.getCode());
                intraDayService.updateIntervalData(weeklyData, stock, IntervalType.WEEKLY);

                List<IntraDayResponse> monthlyData = intraDayService.fetchIntraDayDataByMonth(stock.getCode());
                intraDayService.updateIntervalData(monthlyData, stock, IntervalType.MONTHLY);
                // API 제한 방지용 딜레이
                Thread.sleep(200); // 0.2초 대기
            }
            log.info("단기 데이터 업데이트 완료");
        } catch (Exception e) {
            log.error("단기 데이터 업데이트 중 오류: {}", e.getMessage());
            throw new DomainException(DomainErrorCode.API_FAILED);
        }
    }

    // 2. 매월 1일 새벽 2시 - 1년 데이터 (월별 12개 포인트)
    @Scheduled(cron = "0 0 2 1 * *")
    @Transactional
    public void updateYearlyData() {
        log.info("연간 데이터 업데이트 시작");
        try {
            List<Stock> allStocks = stockRepository.findAll();
            for (Stock stock : allStocks) {
                List<IntraDayResponse> yearlyData = intraDayService.fetchIntraDayDataByYear(stock.getCode());
                intraDayService.updateIntervalData(yearlyData, stock, IntervalType.YEARLY);
                Thread.sleep(300); // 0.3초 대기
            }
            log.info("연간 데이터 업데이트 완료");
        } catch (Exception e) {
            log.error("연간 데이터 업데이트 중 오류: {}", e.getMessage(), e);
            throw new DomainException(DomainErrorCode.API_FAILED);
        }
    }

    // 3. 매년 1월 1일 새벽 3시 - 5년 데이터 (연별 5개 포인트)
    @Scheduled(cron = "0 0 3 1 1 *")
    @Transactional
    public void updateFiveYearData() {
        log.info("5년 데이터 업데이트 시작");
        try {
            List<Stock> allStocks = stockRepository.findAll();
            for (Stock stock : allStocks) {
                List<IntraDayResponse> fiveYearData = intraDayService.fetchIntraDayDataBy5Year(stock.getCode());
                intraDayService.updateIntervalData(fiveYearData, stock, IntervalType.FIVEYEARLY);
                Thread.sleep(250); // 0.25초 대기
            }
            log.info("5년 데이터 업데이트 완료");

        } catch (Exception e) {
            log.error("5년 데이터 업데이트 중 오류: {}", e.getMessage(), e);
            throw new DomainException(DomainErrorCode.API_FAILED);

        }
    }

    // 4. 매년 1월 1일 새벽 4시 - 전체 데이터 (연별 15개 포인트)
    @Scheduled(cron = "0 0 4 1 1 *")
    @Transactional
    public void updateTotalData() {
        log.info("전체 데이터 업데이트 시작 (15년)");
        try {
            List<Stock> allStocks = stockRepository.findAll();

            for (Stock stock : allStocks) {
                List<IntraDayResponse> totalData = intraDayService.fetchIntraDayDataByTotal(stock.getCode());
                intraDayService.updateIntervalData(totalData, stock, IntervalType.TOTAL);

                Thread.sleep(250); // 0.25초 대기 (15번 호출)
            }
            log.info("전체 데이터 업데이트 완료");

        } catch (Exception e) {
            log.error("전체 데이터 업데이트 중 오류: {}", e.getMessage(), e);
            throw new DomainException(DomainErrorCode.API_FAILED);
        }
    }
}
