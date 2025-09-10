package com.fintory.infra.domain.stock.service.korean;


import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.korean.response.KoreanLiveStockPriceResponse;
import com.fintory.domain.stock.dto.korean.response.KoreanRankResponse;
import com.fintory.domain.stock.dto.korean.response.KoreanStockPriceHistoryResponse;
import com.fintory.domain.stock.model.LiveStockPrice;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.model.StockRank;
import com.fintory.domain.stock.service.korean.*;
import com.fintory.infra.domain.stock.repository.LiveStockPriceRepository;
import com.fintory.infra.domain.stock.repository.StockRankRepository;
import com.fintory.infra.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class KoreanStockServiceImpl implements KoreanStockService {

    private final KoreanStockRankService koreanStockRankService;
    private final KoreanLiveStockPriceService koreanLiveStockPriceService;
    private final KoreanStockPriceHistoryService koreanStockPriceHistoryService;

    private final StockRankRepository stockRankRepository;
    private final LiveStockPriceRepository liveStockPriceRepository;
    private final StockRepository stockRepository;

    // 어플리케이션이 완전히 준비된 후 한번만 실행됨
    @EventListener(ApplicationReadyEvent.class)
    public void init(){
       log.info("국내 주식 데이터 초기화 시작");
       initializeAllStockData();
       log.info("국내 주식 데이터 초기화 완료");
    }

    @Scheduled(cron = "0 35 15 * * MON-FRI") // 평일 15:35 (장마감 5분 후)
    public void refreshAfterMarketClose() {
        log.info("장마감 후 주식 데이터 갱신 시작");
        initializeAllStockData();
        log.info("장마감 후 주식 데이터 갱신 완료");
    }


    private void initializeAllStockData() {
        executeWithErrorHandling("주식 랭킹",this::initiateStockRankWithRetry);
        sleepSafely(3000);
        executeWithErrorHandling("현재가 데이터",this::initiateLiveStockPriceWithRetry);
        sleepSafely(3000);
        executeWithErrorHandling("기간별 시세",this::initiateStockPriceHistoryWithRetry);
        sleepSafely(3000);
    }


    private void initiateStockRankWithRetry() {
        koreanStockRankService.initiateKoreanStockRank();
    }

    private void initiateLiveStockPriceWithRetry() {
        koreanLiveStockPriceService.initLiveStockPrice();
    }

    private void initiateStockPriceHistoryWithRetry() {
        koreanStockPriceHistoryService.initiateStockPriceHistory();
    }


    //시가 총액 순위 조회
    @Override
    public List<KoreanRankResponse> getKoreanMarketCapTop20() {
        List<Object[]> results = stockRankRepository.findMarketCapTop20("KRW");
        return mapToKoreanRankResponse(results,StockRank::getMarketCapRank);
    }

    //등락률 순위 조회
    @Override
    public List<KoreanRankResponse> getKoreanROCTop20() {
        List<Object[]> results = stockRankRepository.findROCTop20("KRW");
        return mapToKoreanRankResponse(results,StockRank::getRocRank);
    }

    //거래량 순위 조회
    @Override
    public List<KoreanRankResponse> getKoreanTradingVolumeTop20() {
        List<Object[]> results = stockRankRepository.findTradingVolumeTop20("KRW");
        return mapToKoreanRankResponse(results,StockRank::getTradingVolumeRank);
    }


    //기간별 시세 데이터 조회
    @Override
    public KoreanStockPriceHistoryResponse getKoreanStockPriceHistory(String code){
        return koreanStockPriceHistoryService.getKoreanStockPriceHistory(code);
    }

    //현재가 데이터 조회
    @Override
    public KoreanLiveStockPriceResponse getLiveStockPrice(String code) {
        Stock stock = stockRepository.findByCode(code).orElseThrow(() -> new DomainException(DomainErrorCode.STOCK_NOT_FOUND));
        return koreanLiveStockPriceService.getLiveStockPriceViaQuery(stock);
    }


    private List<KoreanRankResponse> mapToKoreanRankResponse(List<Object[]> results, Function<StockRank, Integer> rankExtractor){
        return results.stream()
                .map(result-> {
                    StockRank stockRank = (StockRank) result[0];
                    LiveStockPrice liveStockPrice = (LiveStockPrice) result[1];

                    return new KoreanRankResponse(
                            stockRank.getStock().getName(),
                            stockRank.getStock().getCode(),
                            rankExtractor.apply(stockRank),
                            liveStockPrice.getCurrentPrice(),
                            liveStockPrice.getPriceChange(),
                            liveStockPrice.getPriceChangeRate()
                    );
                })
                .collect(Collectors.toList());
    }

    // 모든 재시도 로직이 실패했을 때 다음 초기화 메서드를 실행시키기 위해서 만든 메소드
    private boolean executeWithErrorHandling(String taskName,Runnable task){
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
