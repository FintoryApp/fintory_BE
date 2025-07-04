package com.fintory.child.stock.service;

import com.fintory.common.exception.BaseException;
import com.fintory.common.exception.ErrorCode;
import com.fintory.domain.stock.model.Stock;
import com.fintory.infra.repositoryimpl.StockRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl {

    private WebDriver driver;
    private final StockRepository stockRepository;

    private static final String koreanStockUrl = "https://kr.tradingview.com/markets/stocks-korea/market-movers-large-cap/";
    private static final String tokenUrl = "https://openapivts.koreainvestment.com:29443/oauth2/tokenP";


    // 국내 stock 주식 종목 웹스크래핑 - 시가총액 20순위
    public List<String> getKoreanStockMarketCapTop20() throws IOException {
        List<String> KoreanStockMarketCapTop20 = new ArrayList<>();
        setWebDriver();
        try {
            driver.get(koreanStockUrl);

            List<WebElement> lists = driver.findElements(By.cssSelector("a.apply-common-tooltip.tickerNameBox-GrtoTeat.tickerName-GrtoTeat"));
            System.out.println(lists);
            for (int i = 0; i < 20; i++) {
                System.out.println(lists.get(i).getText());
                KoreanStockMarketCapTop20.add(lists.get(i).getText());
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return KoreanStockMarketCapTop20;
    }

    //국내 주식 종목 저장
    public List<Stock> saveKoreanStockMarketCapTop20(List<String> KoreanStockMarketCapTop20) {
        List<String> inStocks = stockRepository.findByInCodeList(KoreanStockMarketCapTop20);
        List<String> notInStocks = new ArrayList<>();
        for(String stock : KoreanStockMarketCapTop20){
            if(!inStocks.contains(stock)){
                notInStocks.add(stock);
            }
        }
        stockRepository.saveAll(stocks);
        return stocks;
    }

    //기본 조회
    public void getStockInfo(List<String> codelists){

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

    // 토큰 발급
    public String getAccessToken(){

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("User-Agent","Mozilla/5.0");

        MultiValueMap<String,String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","client_credentials");
        body.add("appkey",appkey);
        body.add("appsecret",appsecret);

        HttpEntity<MultiValueMap<String,String>> request = new HttpEntity<>(body,headers);

        try{
            ResponseEntity<String> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            if(response.getStatusCode()==HttpStatus.OK && response.getBody() != null){
                return (String) response.getBody().get("access_token");
            }

        }catch(Exception e){
            throw new BaseException(ErrorCode.TOKEN_REQUEST_FAILED);
        }

    }

}
