package com.fintory.infra.domain.stock.service;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.EODResponse;
import com.fintory.domain.stock.dto.EODResponseWrapper;
import com.fintory.domain.stock.model.LiveStockPrice;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.model.StockRank;
import com.fintory.domain.stock.service.StockDataService;
import com.fintory.infra.domain.stock.repository.LiveStockPriceRepository;
import com.fintory.infra.domain.stock.repository.StockRankRepository;
import com.fintory.infra.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
REVIEW EOD 데이터를 이용해서 DB에 값을 저장한다는 점에서 StockRank와 LiveStockPrice를 같은 클래스 내에 위치시킴.
    (EOD 데이터를 받아오면 -> LiveStockPrice, StockRank를 바로 저장하기 위해서)
* */
@Service
@Slf4j
@RequiredArgsConstructor
public class EODataServiceImpl implements StockDataService {

    private final RestTemplate restTemplate;
    private final StockRepository stockRepository;
    private final LiveStockPriceRepository liveStockPriceRepository;
    private final StockRankRepository stockRankRepository;

    @Value("${market-stack.access-key}")
    private String accessKey;

    //최초 초기화 함수
    @Override
    public void getAllEOD() {
        try {
            String stockCodes = getStockCodes();
            log.info(stockCodes);
            EODResponseWrapper wrapper = fetchEODData(stockCodes);
            processStockData(wrapper);
            //REVIEW API를 사용 시 발생할 수 있는 에러들이 많아서(Doc에 명시됨) 광범위하게 범위를 결정함 -> 좋은 방법 찾아보기.
        } catch (Exception e) {
            log.error("EOD 조회 중 에러 발생: {}", e);
            e.printStackTrace();
            throw new DomainException(DomainErrorCode.API_FAILED);
        }
    }

    //WARN 시가 총액의 경우 -> 현재 사용하는 API 등급으로는 접근할 수 없음. 그러나 현재 주식 종목들의 시가총액의 변동성이 크지 않은 점을 고려하여(인기 종목 20개씩) 시가총액은 주식 데이터와 함께 "미리" 저장되는 구조로 진행함.
    //거래량, 전일대비 등락률을 보고 rank 결정
    @Override
    @Transactional
    public void saveAllRank(){
        //국내와 해외 데이터는 순위를 다르게 지정하기 위해서 각각 저장.
        List<StockRank> koreanStockRankList =  stockRankRepository.findByCurrencyName("KRW");
        List<StockRank> overseasStockRankList =  stockRankRepository.findByCurrencyName("USD");

        saveEachRank(koreanStockRankList);
        saveEachRank(overseasStockRankList);
    }

    private void saveEachRank(List<StockRank> stockRankList){
        //거래량 순위 저장
        AtomicInteger volume = new AtomicInteger(1);
        stockRankList.stream()
                .sorted(Comparator.comparing(StockRank::getTradingVolume).reversed()
                        .thenComparing(StockRank::getMarketCap))// 값이 동일하면 시가 총액으로 순위 결정
                .forEach(rank ->{
                    rank.updateVolumeStockRank(volume.getAndIncrement());
                });

        // 등락률 순위 저장
        AtomicInteger roc =new AtomicInteger(1);
        stockRankList.stream()
                .sorted(Comparator.comparing((StockRank rank)-> rank.getRocRate().abs()).reversed()
                        .thenComparing(StockRank::getMarketCapRank)) // 값이 동일하면 시가 총액으로 순위 결정
                .forEach(rank->{
                    rank.updateROCStockRank(roc.getAndIncrement());
                });

        stockRankRepository.saveAll(stockRankList);
    }

    // 전체 주식 코드 조회
    // 개별 API 요청이 아닌 한번에 보내기 위해 주식 code를 처리하는 로직( ex) AAPL,GOOGL....)
    private String getStockCodes() {
        List<Stock> koreanList = stockRepository.findByCurrencyName("KRW");
        List<Stock> overseasList = stockRepository.findByCurrencyName("USD");

        return Stream.concat(koreanList.stream(), overseasList.stream())
                .map(Stock::getCode)
                .collect(Collectors.joining(","));
    }

    // RestTemplate을 통한 MarketStack API 서버와 통신하는 메소드
    private EODResponseWrapper fetchEODData(String symbols) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(7); // 장이 열리지 않는 날을 고려하여 여유 있는 날짜 범위 선택

        String url = UriComponentsBuilder.fromUriString("http://api.marketstack.com/v1/eod")
                .queryParam("access_key", accessKey)
                .queryParam("symbols", symbols)
                .queryParam("sort", "DESC")
                .queryParam("date_from", yesterday)
                .queryParam("date_to", today)
                .toUriString();

        EODResponseWrapper wrapper = restTemplate.getForObject(url, EODResponseWrapper.class);

        if (wrapper == null) {
            throw new DomainException(DomainErrorCode.API_RESPONSE_NULL);
        }

        return wrapper;
    }

    // EOD API를 통해 받은 데이터를 liveStockPrice, StockRank Entity에 저장하는 통합 메소드
    // NOTE 해당 기능을 구현할 구현체도 없고 다른 모듈에서도 사용하지 않지만 같은 트랜잭션을 보장하기 위해 public으로 설정함(인터페이스에선 없음 참고)
    //@PostConstruct -> 트랜잭션 범위 밖에서 실행됨. hibernate 세션이 없어서 Loading 실패
    @Transactional
    public void processStockData(EODResponseWrapper wrapper) {
        if (wrapper == null || wrapper.eodResponseList() == null || wrapper.eodResponseList().isEmpty()) {
            log.warn("API 응답이 비어있어서 처리를 건너뜀");
            return;
        }

        updateLiveStockPrices(wrapper);
        updateStockRanks(wrapper);
    }

    private void updateLiveStockPrices(EODResponseWrapper wrapper) {
        List<Stock> stockList = stockRepository.findAll();

        //미리 주식별로 그룹핑 -> for문 개선
        Map<String,List<EODResponse>> responseMap = wrapper.eodResponseList().stream()
                .collect(Collectors.groupingBy(EODResponse::symbol));

        for (Stock stock : stockList) {
            List<EODResponse> stockResponse = responseMap.getOrDefault(stock.getCode(),List.of());

            if (stockResponse.size() >= 2) {
                saveEachLiveStockPrice(stockResponse, stock);
            } else {
                log.warn("주식 {} - 데이터 부족으로 LiveStockPrice 업데이트 건너뜀 (받은 데이터: {}개) ",
                        stock.getCode(), stockResponse.size());
                //TODO 데이터가 2개 미만이라고 에러를 던지진 않음 -> 일단 현재 서버는 이전 서버와는 달리 에러를 거의 던지지 않고,
                //  만약 데이터를 못가져왔다고 해도 DB에서 이전에 저장된 데이터를 보여주면 되니까. -> 해당 방법 고민해보기
            }
        }
    }

    private void updateStockRanks(EODResponseWrapper wrapper) {
        List<StockRank> stockRankList = stockRankRepository.findAllStockRanks();

        for (StockRank rank : stockRankList) {
            List<EODResponse> stockResponse = wrapper.eodResponseList().stream()
                    .filter(response -> response.symbol().equals(rank.getStock().getCode()))
                    .toList();

            if (stockResponse.size() >= 2) {
                saveStockRankData(stockResponse, rank.getStock());
            } else {
                log.warn("주식 {} - 데이터 부족으로 StockRank 업데이트 건너뜀 (받은 데이터: {}개)",
                        rank.getStock().getCode(), stockResponse.size());
            }
        }
    }

    // 개별 LiveStockPrice 데이터 저장하는 메소드
    private void saveEachLiveStockPrice(List<EODResponse> response,Stock stock){
        LiveStockPrice existing = liveStockPriceRepository.findByStock(stock)
                .orElseGet(()->
                        LiveStockPrice.builder()
                                .stock(stock)
                                .currentPrice(BigDecimal.ZERO)
                                .priceChange(BigDecimal.ZERO)
                                .priceChangeRate(BigDecimal.ZERO)
                                .build());

        existing.updateLiveStockPrice(response);

        liveStockPriceRepository.save(existing);
    }

    //개별 StockRank 데이터 저장하는 메소드
    private void saveStockRankData(List<EODResponse> response,Stock stock){
        StockRank existing = stockRankRepository.findByStock(stock)
                .orElseGet(()->
                        StockRank.builder()
                                .stock(stock)
                                .build());

        existing.updateVolumeAndROC(response);

        stockRankRepository.save(existing);
    }
}
