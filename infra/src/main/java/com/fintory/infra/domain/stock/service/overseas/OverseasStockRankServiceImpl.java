package com.fintory.infra.domain.stock.service.overseas;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OverseasStockRankServiceImpl implements OverseasStockRankService {

    private final StockRepository stockRepository;
    private final StockRankRepository stockRankRepository;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${hantu-openapi.appkey}")
    private String appkey;

    @Value("${hantu-openapi.appsecret}")
    private String appsecret;

    @Value("${hantu-openapi.base-url}")
    private String baseUrl;

    //NOTE 랭킹 데이터는 상대적 비교가 필요해서 전체적인 일관성이 필요함 -> 랭킹이 동일하더라도 프론트에서 받은 데이터를 정렬해서 표시
    @Override
    @Transactional //NOTE 너무 큰 트랜잭션 -> 타임아웃 추가 가능성 열어두기
    public void initiateOverseasStockRank(){
        List<Stock> stockList = stockRepository.findByCurrencyName("USD");
        String token = (String) redisTemplate.opsForValue().get("kis-access-token");
        int successCount = 0;

        if (token == null || token.trim().isEmpty()) {
            log.error("KIS 액세스 토큰을 찾을 수 없습니다.");
            throw new DomainException(DomainErrorCode.TOKEN_EMPTY);
        }

        for(Stock stock : stockList){
            try {
                processStockRankData(stock.getCode(), token);
                successCount++;
            }catch(Exception e){
                log.warn("주식 {} 처리 실패: {}", stock.getCode(), e.getMessage()); //로그 기록 남기기
            }
        }

        //하나라도 성공을 못 시킬때만 재시작
        if(successCount == 0){
            log.error("순위 데이터 초기화 작업 중 모든 종목 처리 실패");
            throw new DomainException(DomainErrorCode.COMPLETE_INITIALIZATION_FAILURE);
        }

        // 순위 데이터 생성 및 저장
        processStockRank();
    }

    //순위를 얻는데 필요한 데이터 조회
    private void processStockRankData(String code, String token) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .path("/uapi/overseas-price/v1/quotations/price-detail")
                    .queryParam("AUTH", "")
                    .queryParam("EXCD", "NAS")
                    .queryParam("SYMB", code)
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("authorization", "Bearer " + token);
            headers.set("appkey", appkey);
            headers.set("appsecret", appsecret);
            headers.set("tr_id", "HHDFS76200200");
            headers.set("custtype", "P");
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                OverseasStockRankDataWrapper wrapper = objectMapper.readValue(response.getBody(), OverseasStockRankDataWrapper.class);
                saveStockRankData(code, wrapper); //db에 데이터 저장
            } else {
                log.error("순위 관련 데이터 조회 실패: {} - 응답이 비어있음", code);
                throw new DomainException(DomainErrorCode.API_RESPONSE_EMPTY);
            }

        } catch (DomainException e) {
            throw e;
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 실패: {} - {}", code, e.getMessage());
            throw new DomainException(DomainErrorCode.JSON_PARSING_ERROR);
        } catch (ResourceAccessException e) {
            log.error("API 연결 실패: {} - {}", code, e.getMessage());
            throw new DomainException(DomainErrorCode.API_CONNECTION_ERROR);
        } catch (Exception e) {
            log.error("예상치 못한 오류: {} - {}", code, e.getMessage());
            throw new DomainException(DomainErrorCode.INTERNAL_SERVER_ERROR);
        }
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
        Stock stock = stockRepository.findByCode(code).orElseThrow(() -> new DomainException(DomainErrorCode.STOCK_NOT_FOUND));

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
        List<StockRank> stockRankList = stockRankRepository.findByCurrencyName("USD");

        stockRankList.sort(Comparator.comparing(StockRank::getMarketCap).reversed());
        for (int i = 0; i < stockRankList.size(); i++) {
            stockRankList.get(i).updateMarketCapRank(i + 1);
        }

        stockRankList.sort(Comparator.comparing((StockRank sr) -> sr.getRocRate().abs()).thenComparing(StockRank::getRocRate).reversed());
        for (int i = 0; i < stockRankList.size(); i++) {
            stockRankList.get(i).updateRocRank(i + 1);
        }

        stockRankList.sort(Comparator.comparing(StockRank::getTradingVolume).reversed());
        for (int i = 0; i < stockRankList.size(); i++) {
            stockRankList.get(i).updateTradingVolumeRank(i + 1);
        }

        stockRankRepository.saveAll(stockRankList);
    }
}