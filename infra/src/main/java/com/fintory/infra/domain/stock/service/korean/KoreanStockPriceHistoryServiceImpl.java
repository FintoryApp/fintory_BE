package com.fintory.infra.domain.stock.service.korean;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.korean.response.KoreanStockPriceHistoryResponse;
import com.fintory.domain.stock.dto.korean.core.KoreanStockPriceHistory;
import com.fintory.domain.stock.dto.korean.wrapper.KoreanStockPriceHistoryWrapper;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.model.StockPriceHistory;
import com.fintory.domain.stock.service.korean.KoreanStockPriceHistoryService;
import com.fintory.infra.domain.stock.repository.StockPriceHistoryRepository;
import com.fintory.infra.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
@RequiredArgsConstructor
public class KoreanStockPriceHistoryServiceImpl implements KoreanStockPriceHistoryService {

    private final RedisTemplate<Object, Object> redisTemplate;
    private final StockPriceHistoryRepository stockPriceHistoryRepository;
    private final StockRepository stockRepository;

    @Qualifier("kisWebClient")
    private final WebClient kisWebClient;

    @Value("${hantu-openapi.appsecret}")
    private String appsecret;

    @Value("${hantu-openapi.appkey}")
    private String appkey;


    @Override
    @Transactional
    public void initiateStockPriceHistory(){
        try {
            List<Stock> stocks = stockRepository.findByCurrencyName("KRW");

            for (Stock stock : stocks) {
                    List<KoreanStockPriceHistory> koreanStockPriceHistories = getKoreanStockItemChatPriceDay(stock.getCode());
                    if (koreanStockPriceHistories == null || koreanStockPriceHistories.isEmpty()) {
                        log.warn("종목 [{}] 데이터가 비어있음 - 건너뜀", stock.getCode());
                        throw new DomainException(DomainErrorCode.STOCK_CHART_API_UNAVAILABLE);
                    }
                    KoreanStockPriceHistory koreanStockPriceHistory = koreanStockPriceHistories.get(0);
                    StockPriceHistory stockPriceHistory = stockPriceHistoryRepository.findByStock(stock)
                            .orElseGet(() -> StockPriceHistory.builder()
                                    .stock(stock)
                                    .build());

                    stockPriceHistory.updateStockPriceHistory(
                            koreanStockPriceHistory.openPrice(),
                            koreanStockPriceHistory.highPrice(),
                            koreanStockPriceHistory.lowPrice(),
                            koreanStockPriceHistory.closePrice());

                    stockPriceHistoryRepository.save(stockPriceHistory);
            }
        } catch(Exception e) {
            e.printStackTrace();
            log.error("saveStockPriceHistory 전체 실행 중 오류 발생", e);
            throw new DomainException(DomainErrorCode.STOCK_PRICE_HISTORY_SAVE_FAILED);
        }
    }

    /*
     *
     * 차트 데이터 조회
     *
     * */
    private List<KoreanStockPriceHistory> getKoreanStockItemChatPriceDay(String code) {
        LocalDate localDate = LocalDate.now();
        return getKoreanStockItemChatPrice("D", code, localDate, localDate, "0");
    }

    private List<KoreanStockPriceHistory> getKoreanStockItemChatPriceWeek(String code) {
        LocalDate beforeSevenDays = LocalDate.now().minusDays(7);
        LocalDate today = LocalDate.now();
        return getKoreanStockItemChatPrice("D", code, beforeSevenDays, today, "0");
    }

    private List<KoreanStockPriceHistory> getKoreanStockItemChatPrice3Month(String code) {
        LocalDate before3Month = LocalDate.now().minusMonths(3);
        LocalDate today = LocalDate.now();
        return getKoreanStockItemChatPrice("D", code, before3Month, today, "0");
    }

    private List<KoreanStockPriceHistory> getKoreanStockItemChatPriceYear(String code) {
        LocalDate beforeYear = LocalDate.now().minusYears(1);
        LocalDate today = LocalDate.now();
        return getKoreanStockItemChatPrice("W", code, beforeYear, today, "1");
    }

    private List<KoreanStockPriceHistory> getKoreanStockItemChatPrice5Year(String code) {
        LocalDate before5Year = LocalDate.now().minusYears(5);
        LocalDate today = LocalDate.now();
        return getKoreanStockItemChatPrice("M", code, before5Year, today, "1");
    }


    private List<KoreanStockPriceHistory> getKoreanStockItemChatPriceTotal(String code) {
        LocalDate beforeYear = LocalDate.now().minusYears(20);
        LocalDate today = LocalDate.now();
        return getKoreanStockItemChatPrice("Y", code, beforeYear, today, "1");
    }


    @Override
    @Transactional
    public List<KoreanStockPriceHistory> getKoreanStockItemChatPrice(String unit, String code, LocalDate localDate1, LocalDate localDate2, String orgAdjPrc) {
        try {
            String token = (String) redisTemplate.opsForValue().get("kis-access-token");
            return kisWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice")
                            .queryParam("FID_COND_MRKT_DIV_CODE", "UN")
                            .queryParam("FID_INPUT_ISCD", code)
                            .queryParam("FID_INPUT_DATE_1", localDate1)
                            .queryParam("FID_INPUT_DATE_2", localDate2)
                            .queryParam("FID_PERIOD_DIV_CODE", unit)
                            .queryParam("FID_ORG_ADJ_PRC", orgAdjPrc)
                            .build()
                    )
                    .header("authorization", "Bearer " + token)
                    .header("appkey", appkey)
                    .header("appsecret", appsecret)
                    .header("tr_id","FHKST03010100")
                    .header("custtype","P")
                    .retrieve()
                    .bodyToMono(KoreanStockPriceHistoryWrapper.class)
                    .map(chart -> chart.output())
                    .onErrorMap(e-> {
                        log.error("호가 데이터 조회 실패: {} - {}", code, e.getMessage());
                        throw new DomainException(DomainErrorCode.API_RESPONSE_EMPTY);
                    })
                    .block();
        } catch (Exception e) {
            log.error("국내 주식 차트 조회 실패 [{}]: {}", code, e.getMessage());
            throw new DomainException(DomainErrorCode.STOCK_CHART_API_UNAVAILABLE);
        }
    }


    public KoreanStockPriceHistoryResponse getKoreanStockPriceHistory(String code) {
        Map<String,List<KoreanStockPriceHistory>> chartData = new HashMap<>();
        Stock stock = stockRepository.findByCode(code).orElseThrow(()-> new DomainException(DomainErrorCode.STOCK_NOT_FOUND));

        chartData.put("1D", getKoreanStockItemChatPriceDay(code));
        chartData.put("1W", getKoreanStockItemChatPriceWeek(code));
        chartData.put("3M", getKoreanStockItemChatPrice3Month(code));
        chartData.put("1Y", getKoreanStockItemChatPriceYear(code));
        chartData.put("5Y", getKoreanStockItemChatPrice5Year(code));
        chartData.put("total", getKoreanStockItemChatPriceTotal(code));

        return new KoreanStockPriceHistoryResponse(stock.getName(),code,chartData);

    }
}
