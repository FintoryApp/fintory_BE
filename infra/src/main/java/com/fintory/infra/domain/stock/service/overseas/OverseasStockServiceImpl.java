package com.fintory.infra.domain.stock.service.overseas;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.overseas.response.*;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.service.overseas.*;
import com.fintory.infra.domain.stock.repository.StockPriceHistoryRepository;
import com.fintory.infra.domain.stock.repository.StockRankRepository;
import com.fintory.infra.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class OverseasStockServiceImpl implements OverseasStockService {

    private final OverseasStockRankService overseasStockRankService;
    private final OverseasLiveStockPriceService overseasLiveStockPriceService;
    private final OverseasStockPriceHistoryService overseasStockPriceHistoryService;

    private final StockRankRepository stockRankRepository;
    private final StockRepository stockRepository;
    private final StockPriceHistoryRepository stockPriceHistoryRepository;


    @EventListener(ApplicationReadyEvent.class)
    public void init(){
        log.info("해외 주식 데이터 초기화 시작");
        initializeAllStockData();
        log.info("해외 주식 데이터 초기화 완료");
    }

    /**
     * 나스닥 시장 마감 후 데이터를 갱신합니다.
     * 서머타임 기간 (3월 둘째 일요일 ~ 11월 첫째 일요일): 04:05
     */
    @Scheduled(cron = "0 5 4 * 3-11 TUE-SAT", zone = "America/New_York")
    // 서머타임: 04:05 (화-토요일)
    public void refreshAfterNasdaqCloseSummerTime() {
        log.info("나스닥 마감 후 해외 주식 데이터 갱신 시작 (서머타임)");
        initializeAllStockData();
        log.info("나스닥 마감 후 해외 주식 데이터 갱신 완료 (서머타임)");
    }

    /**
     * 나스닥 시장 마감 후 데이터를 갱신합니다.
     * 표준시간 기간 (11월 첫째 일요일 ~ 3월 둘째 일요일): 05:05
     */
    @Scheduled(cron = "0 5 5 * 12,1,2 TUE-SAT", zone = "America/New_York") // 표준시간: 05:05 (화-토요일)
    public void refreshAfterNasdaqCloseStandardTime() {
        log.info("나스닥 마감 후 해외 주식 데이터 갱신 시작 (표준)");
        initializeAllStockData();
        log.info("나스닥 마감 후 해외 주식 데이터 갱신 완료 (표준)");
    }

    private void initializeAllStockData() {

            executeWithErrorHandling("현재가 데이터",this::initiateLiveStockPriceWithRetry);
            sleepSafely(1000);

            executeWithErrorHandling("기간별 시세",this::initiateStockPriceHistoryWithRetry);
            sleepSafely(1000);
    }

    //@Retryable도 프록시 기반 AOP => 내부 자기 호출 + private은 @Retryable 적용x
    public void initiateLiveStockPriceWithRetry() {
        overseasLiveStockPriceService.initLiveStockPrice();
    }

    public void initiateStockPriceHistoryWithRetry() {
        overseasStockPriceHistoryService.initiateStockPriceHistory();
    }


    //시가 총액 순위 조회
    @Override
    public List<OverseasMarketCapResponse> getOverseasMarketCapTop20(){
        List<Stock> results = stockRepository.findByCurrencyName("USD");
        return results.stream()
                .map(result->{
                    OverseasLiveStockPriceResponse response =overseasLiveStockPriceService.getLiveStockPriceViaQuery(result);
                    return new OverseasMarketCapResponse(result.getCode(),result.getName(),result.getMarketCap(),response.currentPrice(),result.getCompanyImageUrl());
                })
                .sorted(Comparator.comparing(OverseasMarketCapResponse::marketCap).reversed())
                .collect(Collectors.toList());
    }

    //등락률 순위 조회
    @Override
    public List<OverseasROCResponse> getOverseasROCTop20(){
        List<Stock> results = stockRepository.findByCurrencyName("USD");
        return results.stream()
                .map(stock->{
                    OverseasLiveStockPriceResponse response =overseasLiveStockPriceService.getLiveStockPriceViaQuery(stock);
                    return new OverseasROCResponse(stock.getCode(),stock.getName(),response.currentPrice(),response.openPrice(),stock.getCompanyImageUrl());
                })
                .collect(Collectors.toUnmodifiableList());
    }


    //기간별 시세 데이터 조회
    @Override
    public OverseasStockPriceHistoryResponse getOverseasStockPriceHistory(String code){
        return overseasStockPriceHistoryService.getOverseasStockPriceHistory(code);
    }

    //현재가 데이터 조회
    @Override
    public OverseasLiveStockPriceResponse getLiveStockPrice(String code) {
        Stock stock = stockRepository.findByCode(code).orElseThrow(() -> new DomainException(DomainErrorCode.STOCK_NOT_FOUND));
        return overseasLiveStockPriceService.getLiveStockPriceViaQuery(stock);
    }


    // 모든 재시도 로직이 실패했을 때 다음 초기화 메서드를 실행시키기 위해서 만든 메소드
    private boolean executeWithErrorHandling(String taskName, Runnable task){
        try{
            task.run();
            log.info("{} 초기화 성공",taskName);
            return true;
        }catch(Exception e){
            log.error("{} 초기화 실패 {}",taskName,e.getMessage());
            return false;
        }
    }

    private void sleepSafely(long milliseconds){
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("초기화 중 인터럽트 발생");
        }
    }

}
