package com.fintory.child.stock.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintory.child.stock.dto.*;
import com.fintory.domain.stock.model.LiveStockPrice;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.model.StockPriceHistory;
import com.fintory.domain.stock.model.StockRank;
import com.fintory.domain.stock.repository.*;
import com.fintory.infra.config.OverseasRealPriceWebSocketHandler;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.sql.ast.tree.expression.Over;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fintory.child.stock.dto.KoreanStockChart.toStockPriceHistory;

@Slf4j
@Service
@RequiredArgsConstructor
public class OverseasStockServiceImpl {


    private WebDriver driver;
    private final StockRepository stockRepository;
    private final LiveStockPriceRepository liveStockPriceRepository;
    private final OrderBookRepository orderBookRepository;
    private final OverseasRealPriceWebSocketHandler  overseasRealPriceWebSocketHandler;
    private final StockRankRepository stockRankRepository;
    private final StockPriceHistoryRepository stockPriceHistoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${hantu-openapi.appkey}")
    private String appkey;

    @Value("${hantu-openapi.appsecret}")
    private String appsecret;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    @Qualifier("kisRealPriceOverseasWebClient")
    private WebClient kisRealPriceOverseasWebClient;

    @Autowired
    @Qualifier("kisItemChartPriceOverseasWebClient")
    private WebClient kisItemChartPriceOverseasWebClient;

    @Autowired
    @Qualifier("kisStockInfoOverseasWebClient")
    private WebClient kisStockInfoOverseasWebClient;

    @Autowired
    @Qualifier("yahooWebClient")
    private WebClient yahooWebClient;


    private static final String overseasStockUrl = "https://kr.tradingview.com/markets/stocks-usa/market-movers-large-cap/";
    private static final String overseasROCStockUrl = "https://kr.tradingview.com/markets/stocks-usa/market-movers-gainers/";
    private static final String overseasTradingVolumeUrl ="https://kr.tradingview.com/screener/";


    @EventListener(ApplicationReadyEvent.class)
    public void intializeStockData(){
        saveTotalOverseasStockMarketCapTop20();
        saveTotalOverseasStockROCTop20();
        saveTotalOverseasStockTradingVolumeTop20();
    }

    /* 매일 오전 8시에 실행 */
    @Scheduled(cron="0 0 8 * * ?")
    public void scheduledStockData(){
        saveTotalOverseasStockMarketCapTop20();
        saveTotalOverseasStockROCTop20();
        saveTotalOverseasStockTradingVolumeTop20();
    }
    // 시가총액 순위 조회하기 - 프론트
    public List<OverseasStockMarketCapTop20> getKoreanMarketCapTop20(){

        List<StockRank> ranks = stockRankRepository.findMarketCapTop20();
        List<OverseasStockMarketCapTop20> results = new ArrayList<>();
        for(StockRank rank : ranks){
            LiveStockPrice liveStockPrice = liveStockPriceRepository.findByStock(rank.getStock());
            results.add(OverseasStockMarketCapTop20.builder()
                    .marketCap(rank.getMarketCap())
                    .code(rank.getStock().getCode())
                    .overseasStockRealPrice(OverseasStockRealPrice.builder()
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
    public List<OverseasStockROCTop20> getKoreanROCTop20(){

        List<StockRank> ranks = stockRankRepository.findROCTop20();

        List<OverseasStockROCTop20> results = new ArrayList<>();
        for(StockRank rank : ranks){
            LiveStockPrice liveStockPrice = liveStockPriceRepository.findByStock(rank.getStock());
            results.add(OverseasStockROCTop20.builder()
                    .riseRate(rank.getRiseRate())
                    .code(rank.getStock().getCode())
                    .overseasStockRealPrice(OverseasStockRealPrice.builder()
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
    public List<OverseasStockTradingVolume> getKoreanTradingVolumeTop20(){

        List<StockRank> ranks = stockRankRepository.findTradingVolumeTop20();
        List<OverseasStockTradingVolume> results = new ArrayList<>();
        for(StockRank rank : ranks){
            LiveStockPrice liveStockPrice = liveStockPriceRepository.findByStock(rank.getStock());
            results.add(OverseasStockTradingVolume.builder()
                    .tradingVolume(rank.getTradingVolume())
                    .code(rank.getStock().getCode())
                    .overseasStockRealPrice(OverseasStockRealPrice.builder()
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


    //해외 주식 웹 스크래핑 - 시가총액
    public List<OverseasStockMarketCapTop20> getOverseasStockMarketCapTop20(){
        List<OverseasStockMarketCapTop20> overseasKoreanStockMarketCapTop20s = new ArrayList<>();
        setWebDriver();
        try {
            driver.get(overseasStockUrl);
            List<WebElement> lists = driver.findElements(By.cssSelector("a.apply-common-tooltip.tickerNameBox-GrtoTeat.tickerName-GrtoTeat"));
            List<WebElement> marketCapLists = driver.findElements(By.cssSelector("td.cell-RLhfr_y4.right-RLhfr_y4:nth-child(2)"));

            int validCount = 0;
            for (int i = 0; i < lists.size() && validCount < 20; i++) {
                String code = lists.get(i).getText();

                // API 조회 가능한지 확인
                if (getOverseasStockInfo(code) != null) {
                    overseasKoreanStockMarketCapTop20s.add(
                            OverseasStockMarketCapTop20.builder()
                                    .code(code)
                                    .marketCap(BigDecimal.valueOf(Long.parseLong(marketCapLists.get(i).getText())))
                                    .build()
                    );
                    validCount++;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return overseasKoreanStockMarketCapTop20s;
    }



    //국내 stock 상위 상승주 주식 종목 웹스크래핑
    public List<OverseasStockROCTop20> getOverseasStockROCTop20(){
        List<OverseasStockROCTop20> overseasStockROCTop20s = new ArrayList<>();
        setWebDriver();
        try {
            driver.get(overseasROCStockUrl);

            List<WebElement> lists = driver.findElements(By.cssSelector("a.apply-common-tooltip.tickerNameBox-GrtoTeat.tickerName-GrtoTeat"));
            List<WebElement> rocLists = driver.findElements(By.cssSelector("td.cell-RLhfr_y4.right-RLhfr_y4:nth-child(2) span"));

            int validCount=0;
            for (int i = 0; i < lists.size() && validCount < 20; i++) {
                if(getOverseasStockInfo(lists.get(i).getText()) != null) {
                    overseasStockROCTop20s.add(
                            OverseasStockROCTop20.builder()
                                    .code(lists.get(i).getText())
                                    .riseRate(BigDecimal.valueOf(Double.parseDouble(rocLists.get(i).getText().replace("%", "").replace(",", ""))))
                                    .build()
                    );
                    validCount++;
                }

            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return overseasStockROCTop20s;
    }


    //해외 주식 종목 웹스크래핑 - 거래량 20순위
    public List<OverseasStockTradingVolume> getOverseasStockTradingVolumeTop20(){
        List<OverseasStockTradingVolume> overseasStockTradingVolumes = new ArrayList<>();
        setWebDriver();
        try {
            driver.get(overseasTradingVolumeUrl);

            // 1. 드롭다운 버튼 클릭
            WebElement dropdownButton = driver.findElement(By.cssSelector("button[class*='activeArea'][class*='wholePill']"));
            dropdownButton.click();

            // 2. 잠시 대기 (드롭다운이 열릴 시간)
            Thread.sleep(1000);

            // 3. 미국 옵션 선택 (텍스트로 찾기)
            WebElement usOption = driver.findElement(By.xpath("//li[contains(text(), '미국')] | //div[contains(text(), '미국')] | //span[contains(text(), '미국')]"));
            usOption.click();


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

            int validCount=0;
            for (int i = 0; i < lists.size() && validCount<20; i++) {
                if(getOverseasStockInfo(lists.get(i).getText()) != null) {
                    overseasStockTradingVolumes.add(
                            OverseasStockTradingVolume.builder()
                                    .code(lists.get(i).getText())
                                    .tradingVolume(BigDecimal.valueOf(Double.parseDouble(tradingVolumeLists.get(i).getText().replace(" M", ""))))
                                    .build()
                    );
                    validCount++;
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return overseasStockTradingVolumes;
    }




    /* 주식 종목 통합 저장 */

    public void saveTotalOverseasStockMarketCapTop20() {
        try{
            List<OverseasStockMarketCapTop20> overseasStockMarketCapTop20s = getOverseasStockMarketCapTop20();
            saveOverseasStockMarketCapTop20(overseasStockMarketCapTop20s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void saveTotalOverseasStockROCTop20() {
        try{
            List<OverseasStockROCTop20> overseasStockROCTop20s = getOverseasStockROCTop20();
            saveOverseasStockROCTop20(overseasStockROCTop20s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void saveTotalOverseasStockTradingVolumeTop20() {
        try{
            List<OverseasStockTradingVolume> overseasStockTradingVolumes = getOverseasStockTradingVolumeTop20();
            saveOverseasStockTradingVolumeTop20(overseasStockTradingVolumes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /* 개별 주식 종목 저장 */



    public void saveOverseasStockMarketCapTop20(List<OverseasStockMarketCapTop20> overseasStockMarketCapTop20s) {
        try {
            List<String> codeList = overseasStockMarketCapTop20s.stream()
                    .map(OverseasStockMarketCapTop20::getCode)
                    .collect(Collectors.toList());

            List<String> inStocks = stockRepository.findByCodeList(codeList);
            List<Stock> notSavedStocks = new ArrayList<>();

            for (OverseasStockMarketCapTop20 item : overseasStockMarketCapTop20s) {
                if (!inStocks.contains(item.getCode())) {
                    OverseasStockInfo stockInfo = getOverseasStockInfo(item.getCode());
                    if (stockInfo != null) {
                        Stock stock = stockInfo.toStock(stockInfo);

                        notSavedStocks.add(stock);

                    }
                }
            }

            if (!notSavedStocks.isEmpty()) {
                stockRepository.saveAll(notSavedStocks);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void saveOverseasStockROCTop20(List<OverseasStockROCTop20> overseasStockROCTop20s) {
        try {
            List<String> codeList = new ArrayList<>();
            for (OverseasStockROCTop20 overseasStockROCTop20 : overseasStockROCTop20s) {
                codeList.add(overseasStockROCTop20.getCode());
            }

            List<String> inStocks = stockRepository.findByCodeList(codeList);
            List<Stock> notSavedStocks = new ArrayList<>();

            for (OverseasStockROCTop20 item : overseasStockROCTop20s) {
                if (!inStocks.contains(item.getCode())) {
                    OverseasStockInfo stockInfo = getOverseasStockInfo(item.getCode());
                    if (stockInfo != null) {
                        Stock stock = stockInfo.toStock(stockInfo);
                        notSavedStocks.add(stock);

                    }
                }
            }

            if (!notSavedStocks.isEmpty()) {
                stockRepository.saveAll(notSavedStocks);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    public void saveOverseasStockTradingVolumeTop20(List<OverseasStockTradingVolume> overseasStockTradingVolumes) {
        try {
            List<String> codeList = new ArrayList<>();
            for (OverseasStockTradingVolume overseasStockTradingVolume : overseasStockTradingVolumes) {
                codeList.add(overseasStockTradingVolume.getCode());
            }

            List<String> inStocks = stockRepository.findByCodeList(codeList);
            List<Stock> notSavedStocks = new ArrayList<>();

            for (OverseasStockTradingVolume item : overseasStockTradingVolumes) {
                if (!inStocks.contains(item.getCode())) {
                    OverseasStockInfo stockInfo = getOverseasStockInfo(item.getCode());
                    if (stockInfo != null) {
                        Stock stock = stockInfo.toStock(stockInfo);
                        notSavedStocks.add(stock);

                    }
                }
            }

            if (!notSavedStocks.isEmpty()) {
                stockRepository.saveAll(notSavedStocks);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }



    public List<OverseasStockRank> getOverseasStockRankTop20() {
        Map<String,OverseasStockRank> stockRankMap = new HashMap<>();

        List<OverseasStockMarketCapTop20> marketCapList = getOverseasStockMarketCapTop20();

        List<OverseasStockROCTop20> rocList = getOverseasStockROCTop20();
        List<OverseasStockTradingVolume> tradingVolumeList = getOverseasStockTradingVolumeTop20();

        for(int i=0;i<marketCapList.size();i++){
            String code = marketCapList.get(i).getCode();
            OverseasStockRank existing = stockRankMap.get(code);

            if(existing==null){
                existing = OverseasStockRank.builder()
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
            OverseasStockRank existing = stockRankMap.get(code);

            if (existing == null) {
                existing = OverseasStockRank.builder()
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
            OverseasStockRank existing = stockRankMap.get(code);

            if (existing == null) {
                existing = OverseasStockRank.builder()
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

        List<OverseasStockRank> overseasStockRanks = getOverseasStockRankTop20();
        List<StockRank> stockRankList = new ArrayList<>();

        for(OverseasStockRank overseasStockRank : overseasStockRanks){
            Stock stock = stockRepository.findByCode(overseasStockRank.getCode()).orElse(null);
            StockRank stockRank = StockRank.builder()
                    .rocRank(overseasStockRank.getRocRank())
                    .marketCapRank(overseasStockRank.getMarketCapRank())
                    .tradingVolumeRank(overseasStockRank.getTradingVolumeRank())
                    .marketCap(overseasStockRank.getMarketCap())
                    .riseRate(overseasStockRank.getRiseRate())
                    .tradingVolume(overseasStockRank.getTradingVolume())
                    .stock(stock)
                    .build();
            stockRankList.add(stockRank);
        }
        stockRankRepository.saveAll(stockRankList);
    }

//    public void saveStockPriceHistory(){
//        List<Stock> stocks = stockRepository.findAll();
//        for(Stock stock : stocks){
//            List<OverseasStockChart> overseasStockCharts = getOverseasStockItemChatPriceDay(stock.getCode());
//
//            if (overseasStockCharts == null || overseasStockCharts.isEmpty()) {
//                continue;
//            }
//
//            OverseasStockChart overseasStockChart = overseasStockCharts.get(0);
//
//            StockPriceHistory oldStockPriceHistory = stockPriceHistoryRepository.findByStock(stock);
//
//            if (oldStockPriceHistory != null) {
//                stockPriceHistoryRepository.delete(oldStockPriceHistory);
//            }
//
//            StockPriceHistory newStockPriceHistory = toStockPriceHistory(OverseasStockChart,stock);
//            stockPriceHistoryRepository.save(newStockPriceHistory);
//        }
//
//
//
//    }






    /*
    *
    * 해외 주식 기본 조회
    *
    *
    * */
    public OverseasStockInfo getOverseasStockInfo(String code) {
        try {
            String token = (String) redisTemplate.opsForValue().get("kis-access-token");
            String data = kisStockInfoOverseasWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/uapi/overseas-price/v1/quotations/search-info")
                            .queryParam("PRDT_TYPE_CD", "512")
                            .queryParam("PDNO", code)
                            .build()
                    )
                    .header("authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode node = objectMapper.readTree(data);

            // API 에러 체크
            if (!"0".equals(node.get("rt_cd").asText())) {
                return null;
            }

            node = node.get("output");
            if (node == null) {
                return null;
            }

            return OverseasStockInfo.builder()
                    .code(code)
                    .name(node.get("prdt_name").asText())
                    .marketName(node.get("tr_mket_name").asText())
                    .engName(node.get("prdt_eng_name").asText())
                    .category(node.get("prdt_clsf_name").asText())
                    .build();
        }catch(Exception e){
            return null;
        }
    }


    /*
    *
    * 차트 데이터 조회
    *
    * */
    public String getOverseasStockItemChatPriceDay2(String code) {
        return yahooWebClient.get()
                .uri("https://query1.finance.yahoo.com/v8/finance/chart/{symbol}?interval={interval}&range={range}",
                        code, "1d", "1Y")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .retrieve()
                .bodyToMono(String.class).block();
    }



    public List<OverseasStockChart> getOverseasStockItemChatPriceDay(String code) {
        try {
            LocalDate localDate = LocalDate.now();
            String token = (String) redisTemplate.opsForValue().get("kis-access-token");
            String data = kisItemChartPriceOverseasWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/uapi/overseas-price/v1/quotations/inquire-daily-chartprice")
                            .queryParam("FID_COND_MRKT_DIV_CODE", "N")
                            .queryParam("FID_INPUT_ISCD", code)
                            .queryParam("FID_INPUT_DATE_1", localDate)
                            .queryParam("FID_INPUT_DATE_2", localDate)
                            .queryParam("FID_PERIOD_DIV_CODE", "D")
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
            List<OverseasStockChart> list = objectMapper.readValue(output2Node.toString(), new TypeReference<List<OverseasStockChart>>() {
            });
            return list;
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return List.of();
    }
    public List<OverseasStockChart> getOverseasStockItemChatPriceWeek(String code) {
        try {
            LocalDate beforeSevenDays = LocalDate.now().minusDays(7);
            LocalDate today = LocalDate.now();
            String token = (String) redisTemplate.opsForValue().get("kis-access-token");
            String data = kisItemChartPriceOverseasWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/uapi/overseas-price/v1/quotations/inquire-daily-chartprice")
                            .queryParam("FID_COND_MRKT_DIV_CODE", "N")
                            .queryParam("FID_INPUT_ISCD", code)
                            .queryParam("FID_INPUT_DATE_1", beforeSevenDays)
                            .queryParam("FID_INPUT_DATE_2", today)
                            .queryParam("FID_PERIOD_DIV_CODE", "D")
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
            return objectMapper.readValue(output2Node.toString(), new TypeReference<List<OverseasStockChart>>() {
            });
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return List.of();
    }

    public List<OverseasStockChart> getOverseasStockItemChatPrice3Month(String code) {
        try {
            LocalDate before3Month = LocalDate.now().minusMonths(3);
            LocalDate today = LocalDate.now();
            String token = (String) redisTemplate.opsForValue().get("kis-access-token");
            String data = kisItemChartPriceOverseasWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/uapi/overseas-price/v1/quotations/inquire-daily-chartprice")
                            .queryParam("FID_COND_MRKT_DIV_CODE", "N")
                            .queryParam("FID_INPUT_ISCD", code)
                            .queryParam("FID_INPUT_DATE_1", before3Month)
                            .queryParam("FID_INPUT_DATE_2", today)
                            .queryParam("FID_PERIOD_DIV_CODE", "D")
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
            return objectMapper.readValue(output2Node.toString(), new TypeReference<List<OverseasStockChart>>() {
            });
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return List.of();
    }

    public List<OverseasStockChart> getOverseasStockItemChatPriceYear(String code) {
        try {
            LocalDate beforeYear = LocalDate.now().minusYears(1);
            LocalDate today = LocalDate.now();
            String token = (String) redisTemplate.opsForValue().get("kis-access-token");
            String data = kisItemChartPriceOverseasWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/uapi/overseas-price/v1/quotations/inquire-daily-chartprice")
                            .queryParam("FID_COND_MRKT_DIV_CODE", "N")
                            .queryParam("FID_INPUT_ISCD", code)
                            .queryParam("FID_INPUT_DATE_1", beforeYear)
                            .queryParam("FID_INPUT_DATE_2", today)
                            .queryParam("FID_PERIOD_DIV_CODE", "W")
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
            return objectMapper.readValue(output2Node.toString(), new TypeReference<List<OverseasStockChart>>() {
            });
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return List.of();
    }

    public List<OverseasStockChart> getOverseasStockItemChatPrice5Year(String code) {
        try {
            LocalDate before5Year = LocalDate.now().minusYears(5);
            LocalDate today = LocalDate.now();
            String token = (String) redisTemplate.opsForValue().get("kis-access-token");
            String data = kisItemChartPriceOverseasWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/uapi/overseas-price/v1/quotations/inquire-daily-chartprice")
                            .queryParam("FID_COND_MRKT_DIV_CODE", "N")
                            .queryParam("FID_INPUT_ISCD", code)
                            .queryParam("FID_INPUT_DATE_1", before5Year)
                            .queryParam("FID_INPUT_DATE_2", today)
                            .queryParam("FID_PERIOD_DIV_CODE", "M")
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
            return objectMapper.readValue(output2Node.toString(), new TypeReference<List<OverseasStockChart>>() {
            });
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return List.of();
    }

    public List<OverseasStockChart> getOverseasStockItemChatPriceTotal(String code) {
        try {
            LocalDate beforeYear = LocalDate.now().minusYears(20);
            LocalDate today = LocalDate.now();
            String token = (String) redisTemplate.opsForValue().get("kis-access-token");
            String data = kisItemChartPriceOverseasWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/uapi/overseas-price/v1/quotations/inquire-daily-chartprice")
                            .queryParam("FID_COND_MRKT_DIV_CODE", "N")
                            .queryParam("FID_INPUT_ISCD", code)
                            .queryParam("FID_INPUT_DATE_1", beforeYear)
                            .queryParam("FID_INPUT_DATE_2", today)
                            .queryParam("FID_PERIOD_DIV_CODE", "Y")
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


            List<OverseasStockChart> lists = objectMapper.readValue(output2Node.toString(), new TypeReference<List<OverseasStockChart>>() {
            });
            for (OverseasStockChart koreanStockChart : lists) {
                System.out.println(koreanStockChart.getHighPrice());
            }
            return lists;
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return List.of();
    }


    /* 현재값 발급 메서드 */

    // 최종 현재값 통합 메서드
    public OverseasStockRealPrice getOverseasStockRealPrice(String code){
        try {
            OverseasStockRealPrice overseasStockRealPrice;
            if (isMarketOpen()) {
                overseasStockRealPrice = getOverseasStockRealPriceByRESTAPI(code);
            } else {
                overseasStockRealPrice = getOverseasStockRealPriceByRESTAPI(code);
            }

            if(overseasStockRealPrice == null){
                Stock stock = stockRepository.findByCode(code).orElse(null);
                LiveStockPrice liveStockPrice = liveStockPriceRepository.findByStock(stock);
                return OverseasStockRealPrice.builder()
                        .code(code)
                        .time(String.valueOf(LocalDateTime.now()))
                        .currentPrice(liveStockPrice.getCurrentPrice())
                        .priceChange(liveStockPrice.getPriceChange())
                        .priceChangeRate(liveStockPrice.getPriceChangeRate())
                        .build();

            }else{
                return overseasStockRealPrice;
            }
        }catch(Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    // 현재값 rest API 메서드
    public OverseasStockRealPrice getOverseasStockRealPriceByRESTAPI(String code){
        try {
            String token = (String) redisTemplate.opsForValue().get("kis-access-token");
            ObjectMapper objectMapper = new ObjectMapper();
            Mono<String> data = kisRealPriceOverseasWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/uapi/overseas-price/v1/quotations/price-detail")
                            .queryParam("EXCD","NAS")
                            .queryParam("SYMB", code)
                            .build()
                    )
                    .header("authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class);

            JsonNode node = objectMapper.readTree(data.block());
            System.out.println(node);
            node = node.get("output");

            //priceChange 계산
            BigDecimal last = new BigDecimal(node.get("last").asText());
            BigDecimal base = new BigDecimal(node.get("base").asText());

            BigDecimal priceChange = last.subtract(base);


            //priceChangeRate 계산
            BigDecimal priceChangeRate = last.subtract(base)
                    .divide(base, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            return OverseasStockRealPrice.builder()
                    .currentPrice(node.get("last").asInt())
                    .code(code)
                    .priceChange(priceChange)
                    .priceChangeRate(priceChangeRate)
                    .time(String.valueOf(LocalDateTime.now()))
                    .build();

        }catch(Exception e){
            log.error(e.getMessage());
            return null;
        }
    }



    //현재값 웹소켓 메서드
    public OverseasStockRealPrice getOverseasStockRealPriceByWebSocket(String code){
        try {

            overseasRealPriceWebSocketHandler.subscribeStock(code);

            Object data = redisTemplate.opsForValue().get("overseas-stock-realprice:"+code);

            if(data instanceof Map){
                Map<String,Object> dataMap = (Map<String,Object>)data;

                return OverseasStockRealPrice.builder()
                        .currentPrice(Integer.valueOf(Math.toIntExact((Long) dataMap.get("currentPrice"))))
                        .priceChange(BigDecimal.valueOf((Long) dataMap.get("priceChange")))
                        .priceChangeRate(BigDecimal.valueOf((Long) dataMap.get("priceChangeRate")))
                        .code((String) dataMap.get("code"))
                        .time((String) dataMap.get("time"))
                        .build();
            }

            return null;

        }catch(Exception e){
            log.error(e.getMessage());
            return null;
        }
    }


    // 크롬 브라우저 자동 설치 및 설정
    public void setWebDriver(){
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

    //썸머타임 적용 고려
    public boolean isMarketOpen(){
        LocalTime now = LocalTime.now();
        return now.isAfter(LocalTime.of(23,30)) && now.isBefore(LocalTime.of(5,0));
    }

}
