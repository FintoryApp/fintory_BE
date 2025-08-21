package com.fintory.infra.domain.stock.service;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.*;
import com.fintory.domain.stock.model.LiveStockPrice;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.model.StockRank;
import com.fintory.domain.stock.service.*;
import com.fintory.infra.domain.stock.repository.LiveStockPriceRepository;
import com.fintory.infra.domain.stock.repository.StockRankRepository;
import com.fintory.infra.domain.stock.repository.StockRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {
    private final SearchStockService  searchStockService;
    private final StockPriceHistoryService stockPriceHistoryService;
    private final StockDataService stockDataService;
    private final StockQueryService stockQueryService;

    private final LiveStockPriceRepository liveStockPriceRepository;
    private final StockRepository stockRepository;
    private final StockRankRepository stockRankRepository;


    //초기화 단계
    //NOTE StockPriceHistory 기간별 시세 초기화 메서드 ->  DB에 데이터가 없을 때만 호출
    public void initiateStockPriceHistory(){
        stockPriceHistoryService.getAllIntraDay();
    }

    // LiveStockPrice와 StockRank 초기화
    @PostConstruct
    public void initiateStockData(){
        log.info("End-Of-Data 데이터 초기화 시작");
        try {
            stockDataService.getAllEOD();
            stockDataService.saveAllRank();
            log.info("End-Of-Data 데이터 초기화 완료");
        } catch (Exception e) {
            log.error("End-Of-Data 데이터 초기화 실패 - 서버는 정상 시작: {}", e.getMessage(), e);
            // 예외를 다시 던지지 않음 → 서버 다운 방지
        }
    }

    @Scheduled(cron="0 0 1 * * *")
    public void dailyInitiateStockData(){
        stockDataService.getAllEOD();
        stockDataService.saveAllRank();
    }

    // 시가 총액 순위 조회
    @Override
    public List<RankResponse> getMarketCapTop20(String currency) {
        List<StockRank> ranks = stockRankRepository.findMarketCapTop20(currency);

        return ranks.stream()
                .map(rank -> {
                    LiveStockPrice liveStockPrice = liveStockPriceRepository.findByStock(rank.getStock()).orElseThrow(()-> new DomainException(DomainErrorCode.LIVE_STOCK_PRICE_NOT_FOUND));
                    return new RankResponse(
                            rank.getStock().getName(),
                            rank.getStock().getCode(),
                            rank.getMarketCapRank(),
                            liveStockPrice.getCurrentPrice(),
                            liveStockPrice.getPriceChange(),
                            liveStockPrice.getPriceChangeRate()
                    );
                })
                .collect(Collectors.toList());
    }


    // 등락률 순위 조회
    @Override
    public List<RankResponse> getROCTop20(String  currency) {
        List<StockRank> ranks = stockRankRepository.findROCTop20(currency);

        return ranks.stream()
                .map(rank -> {
                    LiveStockPrice liveStockPrice = liveStockPriceRepository.findByStock(rank.getStock()).orElseThrow(()-> new DomainException(DomainErrorCode.LIVE_STOCK_PRICE_NOT_FOUND));
                    return new RankResponse(
                            rank.getStock().getName(),
                            rank.getStock().getCode(),
                            rank.getRocRank(),
                            liveStockPrice.getCurrentPrice(),
                            liveStockPrice.getPriceChange(),
                            liveStockPrice.getPriceChangeRate()
                    );
                })
                .collect(Collectors.toList());
    }


    // 주식 거래량 순위 조회
    @Override
    public List<RankResponse> getTradingVolumeTop20(String currency){
        List<StockRank> ranks = stockRankRepository.findTradingVolumeTop20(currency);

        return ranks.stream()
                .map(rank -> {
                    LiveStockPrice liveStockPrice = liveStockPriceRepository.findByStock(rank.getStock()).orElseThrow(()-> new DomainException(DomainErrorCode.LIVE_STOCK_PRICE_NOT_FOUND));
                    return new RankResponse(
                            rank.getStock().getName(),
                            rank.getStock().getCode(),
                            rank.getTradingVolumeRank(),
                            liveStockPrice.getCurrentPrice(),
                            liveStockPrice.getPriceChange(),
                            liveStockPrice.getPriceChangeRate()
                    );
                })
                .collect(Collectors.toList());
    }


    //검색 기능
    @Override
    public List<StockSearchResponse> searchStock(StockSearchRequest stockSearchRequest) {
        return searchStockService.searchStock(stockSearchRequest);
    }

    // 기간별 시세 조회 기능
    public StockPriceHistoryWrapper getOverseasStockPriceHistory(String code){
        Stock stock = stockRepository.findByCode(code).orElseThrow(()-> new DomainException(DomainErrorCode.STOCK_NOT_FOUND));
        return stockQueryService.getStockPriceHistory(stock);
    }


    // 현재가 시세 데이터 조회 기능
    //개별 주식 종목의 현재가 시세 데이터 조회
    public LiveStockPriceResponse getEachLiveStockPrice(String code){
        Stock stock = stockRepository.findByCode(code).orElseThrow(()-> new DomainException(DomainErrorCode.STOCK_NOT_FOUND));
        return stockQueryService.getEachLiveStockPrice(stock);
    }

}
