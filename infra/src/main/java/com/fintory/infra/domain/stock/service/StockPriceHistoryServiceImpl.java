package com.fintory.infra.domain.stock.service;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.stock.dto.IntraDayResponse;
import com.fintory.domain.stock.dto.IntraDayResponseWrapper;
import com.fintory.domain.stock.model.IntervalType;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.model.StockPriceHistory;
import com.fintory.domain.stock.service.StockPriceHistoryService;
import com.fintory.infra.domain.stock.repository.StockPriceHistoryRepository;
import com.fintory.infra.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//TODO 일 데이터 -> 웹소켓으로 저장하고 보여주기.(분단위 현재가 데이터를 얻어올 수 없음(국내 주식))
/*

    TODO 각 기간별 시세 데이터를 가져올 떄 -> 중복되는 데이터가 있을 수 있음. 차라리 한번에 가져와서 처리를 해볼까 고민을 했지만
     15년치 데이터에서 개별 기간(주/월/년)을 조절하면서 가져온 후 데이터를 처리하는 것보다는 각각의 API에게 요청하는 방법 선택  -> 더 좋은 방법이 있는지 고민

 */

@Service
@Slf4j
@RequiredArgsConstructor
public class StockPriceHistoryServiceImpl implements StockPriceHistoryService {
    private final RestTemplate restTemplate;
    private final StockRepository stockRepository;
    private final StockPriceHistoryRepository stockPriceHistoryRepository;

    @Value("${market-stack.access-key}")
    private String accessKey;

    //REVIEW 데이터가 적다고 판단될 시 늘릴 예정
    /**
     * weekly -> 7개 이하
     * monthly -> 30개 이하
     * yearly -> 12개 이하
     * fiveyearly -> 5개 이하
     * total -> 15개 이하
     */

    //최초 실행용
    //NOTE 과도한 API 요청을 사용하므로 해당 함수는 데이터베이스가 없을 때만 사용 주의
    @Override
    @Transactional
    public void getAllIntraDay() {
        try {
            List<Stock> allStocks = stockRepository.findAll(); // 모든 주식 조회

            //REVIEW for문을 돌리지만, 에러가 발생한다면 개별 종목이 아닌 전체적인 에러가 발생한 것이라서 다음과 같이 처리
            for (Stock stock : allStocks) {
                String stockCode = stock.getCode();
                Map<String, Object> response = fetchIntraDayData(stockCode); // 전체 IntervalType의 기간별 시세 데이터를 가져옴
                processStockData(response, stock); //liveStockPrice, stockRank 저장
            }
        } catch (Exception e) {
            log.error("EOD 조회 중 에러 발생: {}", e.getMessage());
            throw new DomainException(DomainErrorCode.API_FAILED);
        }
    }


    // 모은 데이터들을 intervalType과 함께 통합 저장
    private Map<String, Object> fetchIntraDayData(String stockCodes) {
        Map<String, Object> intraResponse = new HashMap<>();

        intraResponse.put("WEEKLY", fetchIntraDayDataByWeek(stockCodes));
        intraResponse.put("MONTHLY", fetchIntraDayDataByMonth(stockCodes));
        intraResponse.put("YEARLY", fetchIntraDayDataByYear(stockCodes));
        intraResponse.put("FIVEYEARLY", fetchIntraDayDataBy5Year(stockCodes));
        intraResponse.put("TOTAL", fetchIntraDayDataByTotal(stockCodes));

        return intraResponse;
    }


    //API로부터 받은 데이터로 DB 초기화
    private void processStockData(Map<String, Object> response, Stock stock) {

        stockPriceHistoryRepository.deleteByStock(stock);

        List<StockPriceHistory> historyList = new ArrayList<>();

        if(response!=null && !response.isEmpty()) {
            for (Map.Entry<String, Object> entry : response.entrySet()) {
                String intervalKey = entry.getKey();

                List<IntraDayResponse> dataList = (List<IntraDayResponse>) entry.getValue();

                for (IntraDayResponse data : dataList) {
                    StockPriceHistory history = StockPriceHistory.builder()
                            .stock(stock)
                            .highPrice(data.high())
                            .closePrice(data.close())
                            .openPrice(data.open())
                            .lowPrice(data.low())
                            .intervalType(mapIntoIntervalType(intervalKey))
                            .date(data.date())
                            .build();

                    historyList.add(history);
                }
            }
        }else{
            log.warn("주식 {} 데이터가 비어있음", stock.getCode());
        }
        if (!historyList.isEmpty()) {
            stockPriceHistoryRepository.saveAll(historyList);
        }
    }

    // 주별 시세 데이터 -> 최대 7개 데이터
    protected List<IntraDayResponse> fetchIntraDayDataByWeek(String symbols) {
        LocalDate today = LocalDate.now();
        LocalDate lastWeek = LocalDate.now().minusDays(7);

        String url = UriComponentsBuilder.fromUriString("http://api.marketstack.com/v2/eod")
                .queryParam("access_key", accessKey)
                .queryParam("symbols", symbols)
                .queryParam("sort", "DESC")
                .queryParam("date_from", lastWeek)
                .queryParam("date_to", today)
                .toUriString();
        IntraDayResponseWrapper wrapper = restTemplate.getForObject(url, IntraDayResponseWrapper.class);
        return wrapper.intraDayResponseList();
    }


    //월별 시세 데이터 -> 최대 30개 데이터
    protected List<IntraDayResponse> fetchIntraDayDataByMonth(String symbols) {
        LocalDate today = LocalDate.now();
        LocalDate lastMonth = LocalDate.now().minusDays(30);

        String url = UriComponentsBuilder.fromUriString("http://api.marketstack.com/v2/eod")
                .queryParam("access_key", accessKey)
                .queryParam("symbols", symbols)
                .queryParam("sort", "DESC")
                .queryParam("date_from", lastMonth)
                .queryParam("date_to", today)
                .toUriString();
        IntraDayResponseWrapper wrapper = restTemplate.getForObject(url, IntraDayResponseWrapper.class);
        return wrapper.intraDayResponseList();
    }


    /* interval_type을 지정할 수 없어서 하루 단위로만 데이터를 가져올 수 있음 -> 구간별로 나누어서 딱 1개의 데이터만 요청(변경 가능. 지금은 API 호출량을 줄이기 위함이였음)  */


    //1년 시세 데이터 -> 최대 12개
    protected List<IntraDayResponse> fetchIntraDayDataByYear(String symbols) {
        List<IntraDayResponse> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        //1년을 12구간으로 나눔 -> 월별 데이터
        for (int i = 0; i < 12; i++) {
            LocalDate targetDate = today.minusMonths(i);

            String url = UriComponentsBuilder.fromUriString("http://api.marketstack.com/v2/eod")
                    .queryParam("access_key", accessKey)
                    .queryParam("symbols", symbols)
                    .queryParam("sort", "DESC")
                    .queryParam("date_from", targetDate.minusDays(3)) //주말 대비 여유
                    .queryParam("date_to", targetDate)
                    .queryParam("limit", "1") //딱 1개만
                    .toUriString();

            IntraDayResponseWrapper wrapper = restTemplate.getForObject(url, IntraDayResponseWrapper.class);
            if (!wrapper.intraDayResponseList().isEmpty()) {
                result.add(wrapper.intraDayResponseList().get(0));
            }
        }

        return result;
    }

    //5년 시세 데이터 -> 최대 5개
    protected List<IntraDayResponse> fetchIntraDayDataBy5Year(String symbols) {
        List<IntraDayResponse> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        //5년을 5구간으로 나눔 -> 년도별 데이터
        for (int i = 0; i < 5; i++) {
            LocalDate targetDate = today.minusYears(i);

            String url = UriComponentsBuilder.fromUriString("http://api.marketstack.com/v2/eod")
                    .queryParam("access_key", accessKey)
                    .queryParam("symbols", symbols)
                    .queryParam("sort", "DESC")
                    .queryParam("date_from", targetDate.minusDays(5)) //주말 대비 여유
                    .queryParam("date_to", targetDate)
                    .queryParam("limit", "1") //딱 1개만
                    .toUriString();

            IntraDayResponseWrapper wrapper = restTemplate.getForObject(url, IntraDayResponseWrapper.class);
            if (!wrapper.intraDayResponseList().isEmpty()) {
                result.add(wrapper.intraDayResponseList().get(0));
            }
        }

        return result;
    }


    //15년 시세 데이터 -> 최대 15개
    protected List<IntraDayResponse> fetchIntraDayDataByTotal(String symbols) {
        List<IntraDayResponse> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        //15년을 15구간으로 나누기 -> 년도별 데이터
        for (int i = 0; i < 15; i++) {
            LocalDate targetDate = today.minusYears(i);

            String url = UriComponentsBuilder.fromUriString("http://api.marketstack.com/v2/eod")
                    .queryParam("access_key", accessKey)
                    .queryParam("symbols", symbols)
                    .queryParam("sort", "DESC")
                    .queryParam("date_from", targetDate.minusDays(7)) //주말 대비 여유
                    .queryParam("date_to", targetDate)
                    .queryParam("limit", "1") //딱 1개만
                    .toUriString();

            IntraDayResponseWrapper wrapper = restTemplate.getForObject(url, IntraDayResponseWrapper.class);
            if (!wrapper.intraDayResponseList().isEmpty()) {
                result.add(wrapper.intraDayResponseList().get(0));
            }
        }

        return result;
    }


    // 기간별 시세 저장 메소드 템플릿
    @Override
    public void updateIntervalData(List<IntraDayResponse> dataList, Stock stock, IntervalType intervalType) {

            // 기존 해당 기간 데이터 삭제
            stockPriceHistoryRepository.deleteByStockAndIntervalType(stock, intervalType);

            // 새 데이터 저장
            if (dataList != null && !dataList.isEmpty()) {
                List<StockPriceHistory> historyList = new ArrayList<>();

                for (IntraDayResponse data : dataList) {
                    StockPriceHistory history = StockPriceHistory.builder()
                            .stock(stock)
                            .highPrice(data.high())
                            .closePrice(data.close())
                            .openPrice(data.open())
                            .lowPrice(data.low())
                            .intervalType(intervalType)
                            .date(data.date())
                            .build();
                    historyList.add(history);
                }

                stockPriceHistoryRepository.saveAll(historyList);
            } else {
                log.warn("주식 {} - {} 데이터가 비어있음", stock.getCode(), intervalType);
            }
    }


    // IntervalType 매핑
    public IntervalType mapIntoIntervalType(String interval){
        return switch (interval.toUpperCase()) {
            case "WEEKLY"->IntervalType.WEEKLY;
            case "MONTHLY"->IntervalType.MONTHLY;
            case "YEARLY"->IntervalType.YEARLY;
            case "FIVEYEARLY"->IntervalType.FIVEYEARLY;
            case "TOTAL"->IntervalType.TOTAL;
            default ->{
                log.error("알 수 없는 intervalType:{}", interval);
                yield IntervalType.WEEKLY;
            }
        };
    }

}