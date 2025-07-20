package com.fintory.child.stock.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintory.child.stock.dto.*;

import com.fintory.domain.stock.model.*;
import com.fintory.domain.stock.repository.*;
import com.fintory.infra.config.PriceQuoteWebSocketHandler;
import com.fintory.infra.config.RealPriceWebSocketHandler;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static com.fintory.child.stock.dto.KoreanPriceQuote.toOrderBook;
import static com.fintory.child.stock.dto.KoreanStockChart.toStockPriceHistory;
import static com.fintory.child.stock.dto.KoreanStockRank.toStockRank;
import static com.fintory.child.stock.dto.KoreanStockRealPrice.toLiveStockPrice;


@Slf4j
@Service
@RequiredArgsConstructor
public class KoreanStockServiceImpl {

    private WebDriver driver;
    private final StockRepository stockRepository;
    private final LiveStockPriceRepository liveStockPriceRepository;
    private final OrderBookRepository orderBookRepository;
    private final StockRankRepository stockRankRepository;
    private final StockPriceHistoryRepository stockPriceHistoryRepository;

    @Autowired
    @Qualifier("kisStockInfoWebClient")
    private WebClient kisStockInfoWebClient;

    @Autowired
    @Qualifier("kisItemChartPriceWebClient")
    private WebClient kisItemChartPriceWebClient;

    @Autowired
    @Qualifier("kisPriceQuoteWebClient")
    private WebClient kisPriceQuoteWebClient;

    @Autowired
    @Qualifier("kisRealPriceWebClient")
    private WebClient kisRealPriceWebClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("realPriceWebSocketHandler")
    private RealPriceWebSocketHandler realPriceWebSocketHandler;

    @Autowired
    @Qualifier("priceQuoteWebSocketHandler")
    private PriceQuoteWebSocketHandler priceQuoteWebSocketHandler;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;


    @Value("${hantu-openapi.appkey}")
    private String appkey;

    @Value("${hantu-openapi.appsecret}")
    private String appsecret;


    private static final String koreanStockUrl = "https://kr.tradingview.com/markets/stocks-korea/market-movers-large-cap/";
    private static final String tradingVolumeUrl = "https://kr.tradingview.com/screener/";
    private static final String koreanROCStockUrl = "https://kr.tradingview.com/markets/stocks-korea/market-movers-gainers/";
    private final Set<String> currentSubscriptions = new HashSet<>();


    //완전한 준비 후에 실행되는 이벤트 리스너
    @EventListener(ApplicationReadyEvent.class)
    public void initializeStockData(){

        //1. 각 순위별 주식 데이터 저장
        saveTotalKoreanStockMarketCapTop20();
        saveTotalKoreanStockROCTop20();
        saveTotalKoreanStockTradingVolumeTop20();

        //호가, 현재가 데이터 저장
        saveTotalLiveStockPrice();
        saveTotalOrderBook();
        log.info("호가,현재가 데이터 저장 완료");

        //2. 모든 순위 정보 통합해서 StockRank 저장
        saveStockRank();
        saveStockPriceHistory();
        log.info("순위 정보 업데이트 완료");
    }

    /* 매일 오전 8시에 실행 */
    @Scheduled(cron="0 0 8 * *  MON-FRI")
    public void scheduledStockData(){

        //1. 각 순위별 주식 데이터 저장
        saveTotalKoreanStockMarketCapTop20();
        saveTotalKoreanStockROCTop20();
        saveTotalKoreanStockTradingVolumeTop20();

        //2. 모든 순위 정보 통합해서 StockRank 저장
        saveStockRank();
        saveStockPriceHistory();
        log.info("순위 정보 업데이트 완료");
    }

    // 5분마다 현재가, 호가 데이터 디비에 저장
    @Scheduled(cron = "0 */5 9-15 * * MON-FRI")
    public void saveLiveStockPriceEvery5Minutes(){
        if(isMarketOpen()) {
            saveTotalLiveStockPrice();
            saveTotalOrderBook();
        }
    }

    public void clearRealPriceWebSocketSubscriptions(){
        if(isMarketOpen()){
            realPriceWebSocketHandler.unsubscribeAll();
            currentSubscriptions.clear();
            log.info("웹소켓 구독 모두 해제");
        }
    }

    // 시가총액 순위 조회하기 - 프론트
    public List<KoreanStockMarketCapTop20> getKoreanMarketCapTop20(){

        List<StockRank> ranks = stockRankRepository.findMarketCapTop20();
        List<KoreanStockMarketCapTop20> results = new ArrayList<>();
        for(StockRank rank : ranks){
            LiveStockPrice liveStockPrice = liveStockPriceRepository.findByStock(rank.getStock());
            results.add(KoreanStockMarketCapTop20.builder()
                    .marketCap(rank.getMarketCap())
                    .code(rank.getStock().getCode())
                    .koreanStockRealPrice(KoreanStockRealPrice.builder()
                            .currentPrice(liveStockPrice.getCurrentPrice())
                            .priceChangeRate(liveStockPrice.getPriceChangeRate())
                            .priceChange(liveStockPrice.getPriceChange())
                            .time(liveStockPrice.getTime())
                            .code(rank.getStock().getCode())
                            .build())
                    .build());
        }
        return results;
    }

    // 등락률 순위 조회하기 - 프론트
    public List<KoreanStockROCTop20> getKoreanROCTop20(){

        List<StockRank> ranks = stockRankRepository.findROCTop20();

        List<KoreanStockROCTop20> results = new ArrayList<>();
        for(StockRank rank : ranks){
            LiveStockPrice liveStockPrice = liveStockPriceRepository.findByStock(rank.getStock());
            results.add(KoreanStockROCTop20.builder()
                    .riseRate(rank.getRiseRate())
                    .code(rank.getStock().getCode())
                    .koreanStockRealPrice(KoreanStockRealPrice.builder()
                            .currentPrice(liveStockPrice.getCurrentPrice())
                            .priceChangeRate(liveStockPrice.getPriceChangeRate())
                            .priceChange(liveStockPrice.getPriceChange())
                            .time(liveStockPrice.getTime())
                            .code(rank.getStock().getCode())
                            .build())
                    .build());
        }
        return results;
    }

    //거래량 순위 조회하기
    public List<KoreanStockTradingVolume> getKoreanTradingVolumeTop20(){

        List<StockRank> ranks = stockRankRepository.findTradingVolumeTop20();
        List<KoreanStockTradingVolume> results = new ArrayList<>();
        for(StockRank rank : ranks){
            LiveStockPrice liveStockPrice = liveStockPriceRepository.findByStock(rank.getStock());
            results.add(KoreanStockTradingVolume.builder()
                    .tradingVolume(rank.getTradingVolume())
                    .code(rank.getStock().getCode())
                    .koreanStockRealPrice(KoreanStockRealPrice.builder()
                            .currentPrice(liveStockPrice.getCurrentPrice())
                            .priceChangeRate(liveStockPrice.getPriceChangeRate())
                            .priceChange(liveStockPrice.getPriceChange())
                            .time(liveStockPrice.getTime())
                            .code(rank.getStock().getCode())
                            .build())
                    .build());
        }
        return results;
    }

    /* 최종 주식 주기적 저장 메소드 */
    public void saveTotalKoreanStockMarketCapTop20() {
        try{
            List<KoreanStockMarketCapTop20> koreanStockMarketCapTop20 = getKoreanStockMarketCapTop20();
            saveKoreanStockMarketCapTop20(koreanStockMarketCapTop20);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveTotalKoreanStockROCTop20() {
        try {
            List<KoreanStockROCTop20> koreanStockROCTop20s = getKoreanStockROCTop20();
            saveKoreanStockROCTop20(koreanStockROCTop20s);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveTotalKoreanStockTradingVolumeTop20() {
        try {
            List<KoreanStockTradingVolume> koreanStockTradingVolumeTop20 = getKoreanStockTradingVolumeTop20();
            saveKoreanStockTradingVolumeTop20(koreanStockTradingVolumeTop20);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }



    /* 웹스크래핑 메서드 모음 */



    // 국내 stock 주식 종목 웹스크래핑 - 시가총액 20순위
    public List<KoreanStockMarketCapTop20> getKoreanStockMarketCapTop20() {
            List<KoreanStockMarketCapTop20> koreanStockMarketCapTop20 = new ArrayList<>();
            setWebDriver();
            try {
                driver.get(koreanStockUrl);
                List<WebElement> lists = driver.findElements(By.cssSelector("a.apply-common-tooltip.tickerNameBox-GrtoTeat.tickerName-GrtoTeat"));
                List<WebElement> marketCapLists = driver.findElements(By.cssSelector("td.cell-RLhfr_y4.right-RLhfr_y4:nth-child(2)"));
                for (int i = 0; i < 20; i++) {
                    KoreanStockRealPrice koreanStockRealPrice = getKoreanStockRealPrice(lists.get(i).getText());
                    koreanStockMarketCapTop20.add(
                            KoreanStockMarketCapTop20.builder()
                                    .code(lists.get(i).getText())
                                    .koreanStockRealPrice(koreanStockRealPrice)
                                    .marketCap(BigDecimal.valueOf(Double.parseDouble(marketCapLists.get(i).getText().replace(" T KRW", ""))))
                                    .build()
                    );
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            return koreanStockMarketCapTop20;
    }

    //국내 stock 상위 상승주 주식 종목 웹스크래핑
    public List<KoreanStockROCTop20> getKoreanStockROCTop20() {
        List<KoreanStockROCTop20> koreanStockROCTop20s = new ArrayList<>();
        setWebDriver();
        try {
            driver.get(koreanROCStockUrl);

            List<WebElement> lists = driver.findElements(By.cssSelector("a.apply-common-tooltip.tickerNameBox-GrtoTeat.tickerName-GrtoTeat"));
            List<WebElement> rocLists = driver.findElements(By.cssSelector("td.cell-RLhfr_y4.right-RLhfr_y4:nth-child(2) span"));
            for (int i = 0; i < 20; i++) {
                KoreanStockRealPrice koreanStockRealPrice = getKoreanStockRealPrice(lists.get(i).getText());
                koreanStockROCTop20s.add(
                        KoreanStockROCTop20.builder()
                                .code(lists.get(i).getText())
                                .koreanStockRealPrice(koreanStockRealPrice)
                                .riseRate(BigDecimal.valueOf(Double.parseDouble(rocLists.get(i).getText().replace("%", ""))))
                                .build()
                );

            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return koreanStockROCTop20s;
    }


    //국내 주식 종목 웹스크래핑 - 거래량 20순위
    public List<KoreanStockTradingVolume> getKoreanStockTradingVolumeTop20() {
        List<KoreanStockTradingVolume> koreanStockTradingVolumeTop20 = new ArrayList<>();
        setWebDriver();
        try {
            driver.get(tradingVolumeUrl);

            WebElement hover = driver.findElement(By.cssSelector("[data-field='Volume|TimeResolution1D'] > .cellWrapper-RfwJ5pFm.cellHover-hdxjpvoX.apply-common-tooltip.right-RfwJ5pFm"
            ));
            Actions actions = new Actions(driver);
            actions.moveToElement(hover).perform();

            Thread.sleep(5000);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-field='Volume|TimeResolution1D']  button")));

            button.click();
            Thread.sleep(10000);

            List<WebElement> lists = driver.findElements(By.cssSelector("a.apply-common-tooltip.tickerNameBox-GrtoTeat.tickerName-GrtoTeat"));
            List<WebElement> tradingVolumeLists = driver.findElements(By.cssSelector("td.cell-RLhfr_y4.right-RLhfr_y4:nth-child(4)"));

            for (int i = 0; i < 20; i++) {
                KoreanStockRealPrice koreanStockRealPrice = getKoreanStockRealPrice(lists.get(i).getText());
                koreanStockTradingVolumeTop20.add(
                        KoreanStockTradingVolume.builder()
                                .code(lists.get(i).getText())
                                .koreanStockRealPrice(koreanStockRealPrice)
                                .tradingVolume(BigDecimal.valueOf(Double.parseDouble(tradingVolumeLists.get(i).getText().replace(" M", ""))))
                                .build()
                );
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return koreanStockTradingVolumeTop20;
    }



    /* 주식 저장 메서드 */

    //국내 주식 종목 저장
    public void saveKoreanStockMarketCapTop20(List<KoreanStockMarketCapTop20> koreanStockMarketCapTop20s) {
        try {
            List<String> codeList = new ArrayList<>();
            for (KoreanStockMarketCapTop20 koreanStockMarketCapTop20 : koreanStockMarketCapTop20s) {
                codeList.add(koreanStockMarketCapTop20.getCode());
            }
            List<String> inStocks = stockRepository.findByCodeList(codeList);
            List<Stock> notSavedStocks = new ArrayList<>();
            for (KoreanStockMarketCapTop20 koreanStockMarketCapTop20 : koreanStockMarketCapTop20s) {
                if (!inStocks.contains(koreanStockMarketCapTop20.getCode())) {
                    KoreanStockInfo koreanStockInfo = getKoreanStockInfo(koreanStockMarketCapTop20.getCode());
                    notSavedStocks.add(KoreanStockInfo.toStock(koreanStockInfo));
                }
            }

            stockRepository.saveAll(notSavedStocks);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    public void saveKoreanStockROCTop20(List<KoreanStockROCTop20> koreanStockROCTop20s) {
        try {
            List<String> codeList = new ArrayList<>();
            for (KoreanStockROCTop20 koreanStockROCTop20 : koreanStockROCTop20s) {
                codeList.add(koreanStockROCTop20.getCode());
            }
            List<String> inStocks = stockRepository.findByCodeList(codeList);
            List<Stock> notSavedStocks = new ArrayList<>();


            for (KoreanStockROCTop20 koreanStockROCTop20 : koreanStockROCTop20s) {
                if (!inStocks.contains(koreanStockROCTop20.getCode())) {
                    KoreanStockInfo koreanStockInfo = getKoreanStockInfo(koreanStockROCTop20.getCode());
                    notSavedStocks.add(KoreanStockInfo.toStock(koreanStockInfo));
                }
            }

            stockRepository.saveAll(notSavedStocks);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void saveKoreanStockTradingVolumeTop20(List<KoreanStockTradingVolume> koreanStockTradingVolumes) {
        try {
            List<String> codeList = new ArrayList<>();
            for (KoreanStockTradingVolume koreanStockTradingVolume : koreanStockTradingVolumes) {
                codeList.add(koreanStockTradingVolume.getCode());
            }
            List<String> inStocks = stockRepository.findByCodeList(codeList);
            List<Stock> notSavedStocks = new ArrayList<>();
            for (KoreanStockTradingVolume koreanStockTradingVolume : koreanStockTradingVolumes) {
                if (!inStocks.contains(koreanStockTradingVolume.getCode())) {
                    KoreanStockInfo koreanStockInfo = getKoreanStockInfo(koreanStockTradingVolume.getCode());
                    notSavedStocks.add(KoreanStockInfo.toStock(koreanStockInfo));
                }
            }

            stockRepository.saveAll(notSavedStocks);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }



    public List<KoreanStockRank> getKoreanStockRankTop20() {
        Map<String,KoreanStockRank> stockRankMap = new HashMap<>();

        List<KoreanStockMarketCapTop20> marketCapList = getKoreanStockMarketCapTop20();

        List<KoreanStockROCTop20> rocList = getKoreanStockROCTop20();
        List<KoreanStockTradingVolume> tradingVolumeList = getKoreanStockTradingVolumeTop20();

        for(int i=0;i<marketCapList.size();i++){
            String code = marketCapList.get(i).getCode();
            KoreanStockRank existing = stockRankMap.get(code);

            if(existing==null){
                existing = KoreanStockRank.builder()
                        .code(code)
                        .marketCapRank(i+1)
                        .marketCap(marketCapList.get(i).getMarketCap())
                        .build();
            }else{
                existing.setMarketCapRank(i+1);
                existing.setMarketCap(marketCapList.get(i).getMarketCap());
            }
            stockRankMap.put(code, existing);

        }

        for(int i=0;i<rocList.size();i++) {
            String code = rocList.get(i).getCode();
            KoreanStockRank existing = stockRankMap.get(code);

            if (existing == null) {
                existing = KoreanStockRank.builder()
                        .code(code)
                        .rocRank(i + 1)
                        .riseRate(rocList.get(i).getRiseRate())
                        .build();
            } else {
                existing.setRocRank(i + 1);
                existing.setRiseRate(rocList.get(i).getRiseRate());
            }
            stockRankMap.put(code, existing);

        }

        for(int i=0;i<tradingVolumeList.size();i++) {
            String code = tradingVolumeList.get(i).getCode();
            KoreanStockRank existing = stockRankMap.get(code);

            if (existing == null) {
                existing = KoreanStockRank.builder()
                        .code(code)
                        .tradingVolumeRank(i + 1)
                        .tradingVolume(tradingVolumeList.get(i).getTradingVolume())
                        .build();
            } else {
                existing.setTradingVolumeRank(i + 1);
                existing.setTradingVolume(tradingVolumeList.get(i).getTradingVolume());
            }
            stockRankMap.put(code, existing);
        }
        return new ArrayList<>(stockRankMap.values());
    }

    public void saveStockRank() {
        stockRankRepository.deleteAll();

        List<KoreanStockRank> koreanStockRanks = getKoreanStockRankTop20();
        List<StockRank> stockRankList = new ArrayList<>();

        for(KoreanStockRank koreanStockRank : koreanStockRanks){
            Stock stock = stockRepository.findByCode(koreanStockRank.getCode()).orElse(null);
            StockRank stockRank = StockRank.builder()
                    .rocRank(koreanStockRank.getRocRank())
                    .marketCapRank(koreanStockRank.getMarketCapRank())
                    .tradingVolumeRank(koreanStockRank.getTradingVolumeRank())
                    .marketCap(koreanStockRank.getMarketCap())
                    .riseRate(koreanStockRank.getRiseRate())
                    .tradingVolume(koreanStockRank.getTradingVolume())
                    .stock(stock)
                    .build();
            stockRankList.add(stockRank);
        }
        stockRankRepository.saveAll(stockRankList);
    }

    public void saveStockPriceHistory(){
        List<Stock> stocks = stockRepository.findAll();
        for(Stock stock : stocks){
            List<KoreanStockChart> koreanStockCharts = getKoreanStockItemChatPriceDay(stock.getCode());

            if (koreanStockCharts == null || koreanStockCharts.isEmpty()) {
                continue;
            }

            KoreanStockChart koreanStockChart = koreanStockCharts.get(0);

            StockPriceHistory oldStockPriceHistory = stockPriceHistoryRepository.findByStock(stock);

            if (oldStockPriceHistory != null) {
                stockPriceHistoryRepository.delete(oldStockPriceHistory);
            }

            StockPriceHistory newStockPriceHistory = toStockPriceHistory(koreanStockChart,stock);
            stockPriceHistoryRepository.save(newStockPriceHistory);
        }



    }



    //주식 기본 조회
    public KoreanStockInfo getKoreanStockInfo(String code) {
        try {
            String token = (String) redisTemplate.opsForValue().get("kis-access-token");
            String data = kisStockInfoWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/uapi/domestic-stock/v1/quotations/search-stock-info")
                            .queryParam("PRDT_TYPE_CD", "300")
                            .queryParam("PDNO", code)
                            .build()
                    )
                    .header("authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode node = objectMapper.readTree(data);
            node = node.get("output");

            String market = getMarketNameByCode(node.get("excg_dvsn_cd").asText());
            String category =node.get("idx_bztp_lcls_cd_name").asText();
            if(category.equals("")){
                category="기타";
            }

            return KoreanStockInfo.builder()
                    .code(code)
                    .name(node.get("prdt_abrv_name").asText())
                    .engName(node.get("prdt_eng_abrv_name").asText())
                    .category(category)
                    .marketName(market)
                    .build();
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }

    }

    /*
    *
    * 차트 데이터 조회
    *
    * */


    public List<KoreanStockChart> getKoreanStockItemChatPriceDay(String code) {
        try {
            LocalDate localDate = LocalDate.now();
            String token = (String) redisTemplate.opsForValue().get("kis-access-token");
            String data = kisItemChartPriceWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice")
                            .queryParam("FID_COND_MRKT_DIV_CODE", "UN")
                            .queryParam("FID_INPUT_ISCD", code)
                            .queryParam("FID_INPUT_DATE_1", localDate)
                            .queryParam("FID_INPUT_DATE_2", localDate)
                            .queryParam("FID_PERIOD_DIV_CODE", "D")
                            .queryParam("FID_ORG_ADJ_PRC", "0")
                            .build()
                    )
                    .header("authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .doOnNext(errorBody -> {
                                        log.error(errorBody);
                                    })
                                    .then(Mono.error(new RuntimeException("기간별 시세(일) 얻으면서 생긴 오류")))
                    )
                    .bodyToMono(String.class)
                    .block();

            JsonNode node = objectMapper.readTree(data);
            JsonNode output2Node = node.get("output2");
            List<KoreanStockChart> list = objectMapper.readValue(output2Node.toString(), new TypeReference<List<KoreanStockChart>>() {
            });
            return list;
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return List.of();
    }

    public List<KoreanStockChart> getKoreanStockItemChatPriceWeek(String code) {
        try {
            LocalDate beforeSevenDays = LocalDate.now().minusDays(7);
            LocalDate today = LocalDate.now();
            String token = (String) redisTemplate.opsForValue().get("kis-access-token");
            String data = kisItemChartPriceWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice")
                            .queryParam("FID_COND_MRKT_DIV_CODE", "UN")
                            .queryParam("FID_INPUT_ISCD", code)
                            .queryParam("FID_INPUT_DATE_1", beforeSevenDays)
                            .queryParam("FID_INPUT_DATE_2", today)
                            .queryParam("FID_PERIOD_DIV_CODE", "D")
                            .queryParam("FID_ORG_ADJ_PRC", "0")
                            .build()
                    )
                    .header("authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .doOnNext(errorBody -> {
                                        log.error(errorBody);
                                    })
                                    .then(Mono.error(new RuntimeException("기간별 시세(일) 얻으면서 생긴 오류")))
                    )
                    .bodyToMono(String.class)
                    .block();

            JsonNode node = objectMapper.readTree(data);
            JsonNode output2Node = node.get("output2");
            JsonNode output1Node = node.get("output1");
            return objectMapper.readValue(output2Node.toString(), new TypeReference<List<KoreanStockChart>>() {
            });
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return List.of();
    }

    public List<KoreanStockChart> getKoreanStockItemChatPrice3Month(String code) {
        try {
            LocalDate before3Month = LocalDate.now().minusMonths(3);
            LocalDate today = LocalDate.now();
            String token = (String) redisTemplate.opsForValue().get("kis-access-token");
            String data = kisItemChartPriceWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice")
                            .queryParam("FID_COND_MRKT_DIV_CODE", "UN")
                            .queryParam("FID_INPUT_ISCD", code)
                            .queryParam("FID_INPUT_DATE_1", before3Month)
                            .queryParam("FID_INPUT_DATE_2", today)
                            .queryParam("FID_PERIOD_DIV_CODE", "D")
                            .queryParam("FID_ORG_ADJ_PRC", "0")
                            .build()
                    )
                    .header("authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .doOnNext(errorBody -> {
                                        log.error(errorBody);
                                    })
                                    .then(Mono.error(new RuntimeException("기간별 시세(일) 얻으면서 생긴 오류")))
                    )
                    .bodyToMono(String.class)
                    .block();

            JsonNode node = objectMapper.readTree(data);
            JsonNode output2Node = node.get("output2");
            JsonNode output1Node = node.get("output1");
            List<KoreanStockChart> lists = objectMapper.readValue(output2Node.toString(), new TypeReference<List<KoreanStockChart>>() {
            });

            return lists;
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return List.of();
    }

    public List<KoreanStockChart> getKoreanStockItemChatPriceYear(String code) {
        try {
            LocalDate beforeYear = LocalDate.now().minusYears(1);
            LocalDate today = LocalDate.now();
            String token = (String) redisTemplate.opsForValue().get("kis-access-token");
            String data = kisItemChartPriceWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice")
                            .queryParam("FID_COND_MRKT_DIV_CODE", "UN")
                            .queryParam("FID_INPUT_ISCD", code)
                            .queryParam("FID_INPUT_DATE_1", beforeYear)
                            .queryParam("FID_INPUT_DATE_2", today)
                            .queryParam("FID_PERIOD_DIV_CODE", "W")
                            .queryParam("FID_ORG_ADJ_PRC", "1")
                            .build()
                    )
                    .header("authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .doOnNext(errorBody -> {
                                        log.error(errorBody);
                                    })
                                    .then(Mono.error(new RuntimeException("기간별 시세(일) 얻으면서 생긴 오류")))
                    )
                    .bodyToMono(String.class)
                    .block();

            JsonNode node = objectMapper.readTree(data);
            JsonNode output2Node = node.get("output2");
            return objectMapper.readValue(output2Node.toString(), new TypeReference<List<KoreanStockChart>>() {
            });
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return List.of();
    }

    public List<KoreanStockChart> getKoreanStockItemChatPrice5Year(String code) {
        try {
            LocalDate before5Year = LocalDate.now().minusYears(5);
            LocalDate today = LocalDate.now();
            String token = (String) redisTemplate.opsForValue().get("kis-access-token");
            String data = kisItemChartPriceWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice")
                            .queryParam("FID_COND_MRKT_DIV_CODE", "UN")
                            .queryParam("FID_INPUT_ISCD", code)
                            .queryParam("FID_INPUT_DATE_1", before5Year)
                            .queryParam("FID_INPUT_DATE_2", today)
                            .queryParam("FID_PERIOD_DIV_CODE", "M")
                            .queryParam("FID_ORG_ADJ_PRC", "1")
                            .build()
                    )
                    .header("authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .doOnNext(errorBody -> {
                                        log.error(errorBody);
                                    })
                                    .then(Mono.error(new RuntimeException("기간별 시세(일) 얻으면서 생긴 오류")))
                    )
                    .bodyToMono(String.class)
                    .block();

            JsonNode node = objectMapper.readTree(data);
            JsonNode output2Node = node.get("output2");
            return objectMapper.readValue(output2Node.toString(), new TypeReference<List<KoreanStockChart>>() {
            });
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return List.of();
    }

    public List<KoreanStockChart> getKoreanStockItemChatPriceTotal(String code) {
        try {
            LocalDate beforeYear = LocalDate.now().minusYears(20);
            LocalDate today = LocalDate.now();
            String token = (String) redisTemplate.opsForValue().get("kis-access-token");
            String data = kisItemChartPriceWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice")
                            .queryParam("FID_COND_MRKT_DIV_CODE", "UN")
                            .queryParam("FID_INPUT_ISCD", code)
                            .queryParam("FID_INPUT_DATE_1", beforeYear)
                            .queryParam("FID_INPUT_DATE_2", today)
                            .queryParam("FID_PERIOD_DIV_CODE", "Y")
                            .queryParam("FID_ORG_ADJ_PRC", "1")
                            .build()
                    )
                    .header("authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .doOnNext(errorBody -> {
                                        log.error(errorBody);
                                    })
                                    .then(Mono.error(new RuntimeException("기간별 시세(일) 얻으면서 생긴 오류")))
                    )
                    .bodyToMono(String.class)
                    .block();

            JsonNode node = objectMapper.readTree(data);
            JsonNode output2Node = node.get("output2");


            List<KoreanStockChart> lists = objectMapper.readValue(output2Node.toString(), new TypeReference<List<KoreanStockChart>>() {
            });

            return lists;
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return List.of();
    }


    // 크롬 브라우저 자동 설치 및 설정
    public void setWebDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
        options.addArguments("--headless");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-dev-tools");        // DevTools 완전 비활성화


        driver = new ChromeDriver(options);
    }

    //시간외 호가 조회 - rest api
    public KoreanPriceQuote getKoreanPriceQuoteByRestAPI(String code) throws JsonProcessingException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String token = (String) redisTemplate.opsForValue().get("kis-access-token");
        String data = kisPriceQuoteWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("uapi/domestic-stock/v1/quotations/inquire-asking-price-exp-ccn")
                        .queryParam("FID_COND_MRKT_DIV_CODE", "UN")
                        .queryParam("FID_INPUT_ISCD", code)
                        .build())
                .header("authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)
                .block();


        JsonNode body = objectMapper.readTree(data);
        KoreanPriceQuote koreanPriceQuote = new KoreanPriceQuote();
        body = body.get("output1");

        koreanPriceQuote.setCode(code);
        for (int i = 1; i < 11; i++) {
            String methodNameASKP = "setASKP" + i;
            String methodNameBIDPRSQN = "setBIDPRSQN" + i;
            String methodNameBIDP = "setBIDP" + i;
            String methodNameASKPRSQN = "setASKPRSQN" + i;
            Method methodASKP = koreanPriceQuote.getClass().getMethod(methodNameASKP, BigDecimal.class);
            Method methodBIDPRSQN = koreanPriceQuote.getClass().getMethod(methodNameBIDPRSQN, Long.class);
            Method methodBIDP = koreanPriceQuote.getClass().getMethod(methodNameBIDP, BigDecimal.class);
            Method methodASKPRSQN = koreanPriceQuote.getClass().getMethod(methodNameASKPRSQN, Long.class);

            methodASKP.invoke(koreanPriceQuote, new BigDecimal(body.get("askp" + i).asText()));
            methodBIDPRSQN.invoke(koreanPriceQuote, Long.valueOf(body.get("bidp_rsqn" + i).asText()));
            methodBIDP.invoke(koreanPriceQuote, new BigDecimal(body.get("bidp" + i).asText()));
            methodASKPRSQN.invoke(koreanPriceQuote, Long.valueOf(body.get("askp_rsqn" + i).asText()));

        }
        return koreanPriceQuote;

    }

    // 호가 조회
    //NoSuchMethodException, InvocationTargetException, IllegalAccessException, JsonProcessingException
    public KoreanPriceQuote getPriceQuote(String code) {
        try {

            KoreanPriceQuote koreanPriceQuote = new KoreanPriceQuote();

            if (!isMarketOpen()) {
                return getKoreanPriceQuoteByRestAPI(code);
            } else {
                priceQuoteWebSocketHandler.subscribe(code);

                Thread.sleep(1000);

                Object cachedData = redisTemplate.opsForValue().get("korean-stock-quote:" + code); //redis는 기본적으로 key-value 문자열 저장소.
                if (cachedData == null) {
                    log.warn("Redis에서 호가 데이터를 찾을 수 없음: {}", code);
                    return getKoreanPriceQuoteByRestAPI(code); // callback
                }
                if (cachedData instanceof Map) {
                    return convertMapToKoreanPriceQuote((Map<String, Object>) cachedData);
                } else {
                    log.error("Redis 데이터 타입 오류: {}", cachedData.getClass());
                    return null;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void saveOrderBook(KoreanPriceQuote koreanPriceQuote) {

        Stock stock = stockRepository.findByCode(koreanPriceQuote.getCode()).orElse(null);
        OrderBook oldOrderBook = orderBookRepository.findByStockId(stock.getId());

        if(oldOrderBook != null) {
            orderBookRepository.delete(oldOrderBook);
        }
        OrderBook newOrderBook = toOrderBook(koreanPriceQuote, stock);

        orderBookRepository.save(newOrderBook);
    }

    public void saveTotalOrderBook(){
        List<Stock> stockList = stockRepository.findAll();
        for(Stock stock : stockList){
            KoreanPriceQuote priceQuote = getPriceQuote(stock.getCode());
            if(priceQuote != null) {
                saveOrderBook(priceQuote);
            }

        }
    }

    public void saveTotalLiveStockPrice(){
        List<Stock> stockList = stockRepository.findAll();
        for(Stock stock : stockList){
            KoreanStockRealPrice realPrice = getKoreanStockRealPrice(stock.getCode());
            saveLiveStockPrice(realPrice);
        }
    }

    public void saveLiveStockPrice(KoreanStockRealPrice koreanStockRealPrice) {
        if(koreanStockRealPrice==null){
            log.warn("korean stock real price를 못 얻어오고 있음");
            return;
        }
        Stock stock = stockRepository.findByCode(koreanStockRealPrice.getCode()).orElse(null);
        LiveStockPrice liveStockPrice = liveStockPriceRepository.findByStock(stock);

        if(liveStockPrice != null) {
            liveStockPriceRepository.delete(liveStockPrice);
        }

        LiveStockPrice newLiveStockPrice = toLiveStockPrice(koreanStockRealPrice, stock);

        liveStockPriceRepository.save(newLiveStockPrice);
    }

    /* 현재가 발급 관련 메서드 */

    // 통합 현재가 발급 메서드 (수정된 버전)
    public KoreanStockRealPrice getKoreanStockRealPrice(String code){
        try {
            KoreanStockRealPrice koreanStockRealPrice = null; // 초기화 추가

            // 장중 시간 -> 실시간 현재가 발급
            if (isMarketOpen()) {
                currentSubscriptions.add(code);
                realPriceWebSocketHandler.subscribeStock(code);

                Object cachedData = redisTemplate.opsForValue().get("korean-stock-realprice:" + code);

                // 웹소켓 데이터가 있으면 사용
                if (cachedData instanceof Map) {
                    Map<String, Object> dataMap = (Map<String, Object>) cachedData;

                    koreanStockRealPrice = KoreanStockRealPrice.builder()
                            .currentPrice(Integer.valueOf((String) dataMap.get("currentPrice")))
                            .priceChange(new BigDecimal((String) dataMap.get("priceChange"))) // BigDecimal 생성자 사용
                            .priceChangeRate(new BigDecimal((String) dataMap.get("priceChangeRate"))) // BigDecimal 생성자 사용
                            .code(code)
                            .time((String) dataMap.get("time"))
                            .build();
                }

                // 웹소켓 데이터 없으면 REST API로 fallback
                if (koreanStockRealPrice == null) {
                    koreanStockRealPrice = getKoreanStockRealPriceByRestAPI(code);
                }

                return koreanStockRealPrice;

            } else {
                // 장외시간 처리
                String cachedKey = "korean-real-price" + code;
                Object cached = redisTemplate.opsForValue().get(cachedKey);

                if (cached != null) {
                    return (KoreanStockRealPrice) cached;
                }

                koreanStockRealPrice = getKoreanStockRealPriceByRestAPI(code);

                if (koreanStockRealPrice != null) {
                    redisTemplate.opsForValue().set(cachedKey, koreanStockRealPrice, Duration.ofMinutes(5)); // 장외는 5분으로 고정
                }

                return koreanStockRealPrice;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    // rest api 현재가 호출
    public KoreanStockRealPrice getKoreanStockRealPriceByRestAPI(String code) throws IOException, InterruptedException {
        String token = (String) redisTemplate.opsForValue().get("kis-access-token");
        Mono<String> data = kisRealPriceWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("uapi/domestic-stock/v1/quotations/inquire-asking-price-exp-ccn")
                        .queryParam("FID_COND_MRKT_DIV_CODE", "UN")
                        .queryParam("FID_INPUT_ISCD", code)
                        .build())
                .header("authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class);


        JsonNode body = objectMapper.readTree(data.block());
        KoreanStockRealPrice realPrice = new KoreanStockRealPrice();
        body = body.get("output");

        realPrice.setCurrentPrice(body.get("stck_prpr").asInt());
        realPrice.setPriceChange(new BigDecimal(body.get("prdy_vrss").asText()));
        realPrice.setPriceChangeRate(new BigDecimal(body.get("prdy_ctrt").asText()));
        realPrice.setTime(String.valueOf(LocalDateTime.now()));
        realPrice.setCode(code);

        return realPrice;
    }

    //실시간 현재가 api 호출
    public KoreanStockRealPrice getKoreanStockRealPriceFromWebSocket(String code) throws IOException, InterruptedException {

        Object cachedData = redisTemplate.opsForValue().get("korean-stock-realprice:" + code);
        if (cachedData == null) {
            return null; // 또는 기본값
        }

        // Map으로 캐스팅
        if (cachedData instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) cachedData;

            return KoreanStockRealPrice.builder()
                    .currentPrice(Integer.valueOf((String) dataMap.get("currentPrice")))
                    .priceChange(BigDecimal.valueOf(Long.parseLong((String) dataMap.get("priceChange"))))
                    .priceChangeRate(BigDecimal.valueOf(Long.parseLong((String) dataMap.get("priceChangeRate"))))
                    .code((String) dataMap.get("code"))
                    .time((String) dataMap.get("time"))
                    .build();
        }

        return null;
    }

    // 특정 주기마다 현재가 update
    // 장중이면 db 저장 빠르게, 장외면 30~1분 간격, 주말 : 5분 간격
    public void saveKoreanStockRealPrice(KoreanStockRealPrice koreanStockRealPrice) {
        Stock stock = stockRepository.findByCode(koreanStockRealPrice.getCode()).orElse(null);
        LiveStockPrice liveStockPrice = liveStockPriceRepository.findByStock(stock);

        liveStockPrice.update(
                koreanStockRealPrice.getPriceChange(),
                koreanStockRealPrice.getPriceChangeRate(),
                koreanStockRealPrice.getCurrentPrice()
        );

        liveStockPriceRepository.save(liveStockPrice);
    }

    private KoreanPriceQuote convertMapToKoreanPriceQuote(Map<String, Object> map) {
        try {
            KoreanPriceQuote quote = new KoreanPriceQuote();
            quote.setCode((String) map.get("code"));

            // 매도호가 설정
            for (int i = 1; i <= 10; i++) {
                String methodName = "setASKP" + i;
                Method method = quote.getClass().getMethod(methodName, BigDecimal.class);
                Object value = map.get("ASKP" + i);
                if (value instanceof BigDecimal) {
                    method.invoke(quote, value);
                }
            }

            // 매수호가 설정
            for (int i = 1; i <= 10; i++) {
                String methodName = "setBIDP" + i;
                Method method = quote.getClass().getMethod(methodName, BigDecimal.class);
                Object value = map.get("BIDP" + i);
                if (value instanceof BigDecimal) {
                    method.invoke(quote, value);
                }
            }

            // 매도호가 잔량 설정
            for (int i = 1; i <= 10; i++) {
                String methodName = "setASKPRSQN" + i;
                Method method = quote.getClass().getMethod(methodName, Long.class);
                Object value = map.get("ASKPRSQN" + i); // 올바른 키 이름
                if (value instanceof Long) {
                    method.invoke(quote, value);
                }
            }

            // 매수호가 잔량 설정
            for (int i = 1; i <= 10; i++) {
                String methodName = "setBIDPRSQN" + i;
                Method method = quote.getClass().getMethod(methodName, Long.class);
                Object value = map.get("BIDPRSQN" + i); // 올바른 키 이름
                if (value instanceof Long) {
                    method.invoke(quote, value);
                }
            }

            return quote;

        } catch (Exception e) {
            log.error("Map to DTO 변환 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    public String getMarketNameByCode(String code) {
        if (code == null) return "기타";

        switch (code.trim()) { //String.valueOf로 하면 따옴표가 포함됨.
            case "01": return "한국증권";
            case "02": return "증권거래소";
            case "03": return "코스닥";
            case "04": return "K-OTC";
            case "05": return "선물거래소";
            case "55": return "미국";
            case "56": return "일본";
            default: return "기타";
        }
    }



    private boolean isMarketOpen(){
        LocalTime now = LocalTime.now();
        return now.isAfter(LocalTime.of(9, 0)) && now.isBefore(LocalTime.of(15,0));
    }
}
