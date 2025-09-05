package com.fintory.infra.domain.stock.service.korean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.korean.core.KoreanLiveStockPrice;
import com.fintory.domain.stock.dto.korean.response.KoreanLiveStockPriceResponse;
import com.fintory.domain.stock.dto.korean.wrapper.KoreanLiveStockPriceWrapper;
import com.fintory.domain.stock.model.LiveStockPrice;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.service.korean.KoreanLiveStockPriceService;
import com.fintory.infra.domain.stock.repository.LiveStockPriceRepository;
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

import java.util.List;


import static com.fintory.domain.stock.dto.korean.response.KoreanLiveStockPriceResponse.convertFromLiveStockPrice;

@Service
@Slf4j
@RequiredArgsConstructor
public class KoreanLiveStockPriceServiceImpl implements KoreanLiveStockPriceService {

    @Value("${hantu-openapi.appkey}")
    private String appkey;

    @Value("${hantu-openapi.appsecret}")
    private String appsecret;

    @Value("${hantu-openapi.base-url}")
    private String baseUrl;

    private final RedisTemplate<Object, Object> redisTemplate;
    private final StockRepository stockRepository;
    private final LiveStockPriceRepository liveStockPriceRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;


    //TODO 웹소켓에서도 주기적 저장. 장외시간일 때 첫 저장.
    //TODO 디비에서 값 조회 메서드
    @Override
    @Transactional
    public void initLiveStockPrice(){
        List<Stock> stockList = stockRepository.findByCurrencyName("KRW");
        String token = (String) redisTemplate.opsForValue().get("kis-access-token");
        int successCount=0;

        if (token == null || token.trim().isEmpty()) {
            log.error("KIS 액세스 토큰을 찾을 수 없습니다.");
            throw new DomainException(DomainErrorCode.TOKEN_EMPTY);
        }

        for(Stock stock : stockList) {
            try {
                getLiveStockPriceViaRestAPI(stock.getCode(), token);
                successCount++;
            } catch (Exception  e) {
                log.warn("주식 {} 처리 실패: {}", stock.getCode(), e.getMessage()); //로그 기록 남기기
            }
        }

        //NOTE 처음에는 하나라도 못 가져오면 retry를 시킬려고 했는데 -> 어차피 db에 저장된 데이터가 있으므로 차라리 많은 데이터를 초기화 시키는 방법이 좋을 것 같아서 변경
        //REVIEW 하나라도 성공을 못시킬 때만 재시작
        if(successCount==0) {
            log.error("현재가 데이터 초기화 작업 중 종목 처리 실패");
            throw new DomainException(DomainErrorCode.COMPLETE_INITIALIZATION_FAILURE);
        }

    }

    @Override
    @Transactional
    public void getLiveStockPriceViaRestAPI(String code, String token){
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .path("/uapi/domestic-stock/v1/quotations/inquire-price")
                    .queryParam("FID_COND_MRKT_DIV_CODE", "UN")
                    .queryParam("FID_INPUT_ISCD", code)
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("authorization", "Bearer " + token);
            headers.set("appkey", appkey);
            headers.set("appsecret", appsecret);
            headers.set("tr_id", "FHKST01010100");
            headers.set("custtype", "P");
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                KoreanLiveStockPriceWrapper wrapper = objectMapper.readValue(response.getBody(), KoreanLiveStockPriceWrapper.class);
                saveLiveStockPrice(code, wrapper.output()); //현재가 데이터 DB에 저장
            } else {
                log.error("현재가 데이터 조회 실패: {} - 응답이 비어있음", code);
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

    //현재가 데이터 저장 메소드
    private void saveLiveStockPrice(String code, KoreanLiveStockPrice priceDto){
        Stock stock = stockRepository.findByCode(code).orElseThrow(()-> new DomainException(DomainErrorCode.STOCK_NOT_FOUND));
        LiveStockPrice liveStockPrice = liveStockPriceRepository.findByStock(stock)
                .orElseGet(() -> LiveStockPrice.builder()
                        .stock(stock)
                        .build());
        liveStockPrice.updateLiveStockPrice(priceDto.currentPrice(),priceDto.priceChange(),priceDto.priceChangeRate());
        liveStockPriceRepository.save(liveStockPrice);
    }

    @Override
    public KoreanLiveStockPriceResponse getLiveStockPriceViaQuery(Stock stock){
        //DB에 저장된 현재가가 없는 것은 @PostConstruct 과정에서 초기화가 제대로 실행이 안되었다는 뜻이므로 live_stock_price 에러 발생
        LiveStockPrice liveStockPrice = liveStockPriceRepository.findByStock(stock).orElseThrow(()-> new DomainException(DomainErrorCode.LIVE_STOCK_PRICE_NOT_FOUND));
        return convertFromLiveStockPrice(liveStockPrice);
    }

}
