package com.fintory.infra.domain.stock.service.overseas;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.overseas.core.OverseasStockRankData;
import com.fintory.domain.stock.dto.overseas.wrapper.OverseasStockRankDataWrapper;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.model.StockRank;
import com.fintory.domain.stock.service.overseas.OverseasStockRankService;
import com.fintory.infra.domain.stock.repository.StockRankRepository;
import com.fintory.infra.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OverseasStockRankServiceImpl implements OverseasStockRankService {


    private final StockRepository stockRepository;
    private final StockRankRepository stockRankRepository;
    private final RedisTemplate<Object, Object> redisTemplate;

    @Qualifier("kisWebClient")
    private final WebClient kisWebClient;

    @Value("${hantu-openapi.appkey}")
    private String appkey;

    @Value("${hantu-openapi.appsecret}")
    private String appsecret;


    //  순위 저장
    @Override
    @Transactional
    public void saveOverseasStockRank(){

        List<Stock> stockList = stockRepository.findByCurrencyName("USD");
        String token = (String) redisTemplate.opsForValue().get("kis-access-token");

        int batchSize=10;

        for(int i=0;i<stockList.size();i+=batchSize){
            List<Stock> batch = stockList.subList(i,Math.min(i+batchSize,stockList.size()));

            List<Mono<Void>> request = batch.stream()
                    .map(stock-> processStockRankData(stock.getCode(),token))
                    .collect(Collectors.toList());


            //배치 단위로 모든 요청 완료까지 대기
            Mono.when(request).block();
        }
        processStockRank();
    }


    //순위를 얻는데 필요한 데이터 조회
    private Mono<Void> processStockRankData(String code, String token) {
        return  kisWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/uapi/domestic-stock/v1/quotations/inquire-price")
                        .queryParam("AUTH","")
                        .queryParam("EXCD", "NAS")
                        .queryParam("SYMB", code)
                        .build())
                .header("authorization", "Bearer "+token)
                .header("appkey", appkey)
                .header("appsecret", appsecret)
                .header("tr_id", "HHDFS76200200")
                .header("custtype", "P")
                .retrieve()
                .bodyToMono(OverseasStockRankDataWrapper.class)
                .doOnNext(rank-> saveStockRankData(code,rank))
                .onErrorMap(e -> {
                    log.error("순위 관련 데이터 조회 실패: {} - {}", code, e.getMessage());
                    throw new DomainException(DomainErrorCode.API_RESPONSE_EMPTY);
                })
                .then();
    }

    //순위를 얻는데 필요한 데이터 저장 메서드
    private void saveStockRankData(String code, OverseasStockRankDataWrapper response) {

        if (response == null || response.output() == null) {
            log.warn("순위 관련 데이터 응답이 비어있음: {}", code);
            throw new DomainException(DomainErrorCode.API_RESPONSE_EMPTY);
        }

        OverseasStockRankData item = response.output();

        if (item == null) {
            log.warn("순위 관련 응답에서 데이터를 찾을 수 없음");
            throw new DomainException(DomainErrorCode.STOCK_DATA_NOT_FOUND);
        }

        StockRank stockRank = stockRankRepository.findByStockCode(code).orElse(null);
        Stock stock = stockRepository.findByCode(code).orElseThrow(()->new DomainException(DomainErrorCode.STOCK_NOT_FOUND));

        if (stockRank == null) {
            stockRank = StockRank.builder()
                    .tradingVolume(item.tradingVolume())
                    .rocRate(item.roc())
                    .marketCap(item.marketCap())
                    .stock(stock)
                    .build();
        } else {
            stockRank.updateStockRankData(item.marketCap(), item.roc(), item.tradingVolume());
        }

        stockRankRepository.save(stockRank);
    }

    //순위 데이터 생성 및 저장
    private void processStockRank(){
        List<StockRank> marketCapRankList = stockRankRepository.findAllOrderByMarketCap("USD");
        List<StockRank> rocRankList = stockRankRepository.findAllOrderByRocRate("USD");
        List<StockRank> tradingVolumeRankList = stockRankRepository.findAllOrderByTradingVolume("USD");


        for (int i = 0; i < marketCapRankList.size(); i++) {
            StockRank stockRank = marketCapRankList.get(i);
            stockRank.updateMarketCapRank(i + 1);
            stockRankRepository.save(stockRank);
        }


        for (int i = 0; i < rocRankList.size(); i++) {
            StockRank stockRank = rocRankList.get(i);
            stockRank.updateRocRank(i + 1);
            stockRankRepository.save(stockRank);
        }


        for (int i = 0; i < tradingVolumeRankList.size(); i++) {
            StockRank stockRank = tradingVolumeRankList.get(i);
            stockRank.updateTradingVolumeRank(i + 1);
            stockRankRepository.save(stockRank);
        }

    }
}
