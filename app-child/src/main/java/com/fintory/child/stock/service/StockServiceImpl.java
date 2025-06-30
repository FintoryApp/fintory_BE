package com.fintory.child.stock.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl {

    private WebDriver driver;

    private static final String koreanStockUrl = "https://kr.tradingview.com/markets/stocks-korea/market-movers-large-cap/";

    // 국내 stock 주식 종목 웹스크래핑 - 시가총액 20순위
    public void getKoreanStockMarketCapTop20() throws IOException {
        List<String> KoreanStockMarketCapTop20 = new ArrayList<>();
        setWebDriver();
        try{
            driver.get(koreanStockUrl);

            List<WebElement> lists = driver.findElements(By.cssSelector("a.apply-common-tooltip.tickerNameBox-GrtoTeat.tickerName-GrtoTeat"));
            System.out.println(lists);
            for(int i=0;i<20;i++){
                System.out.println(lists.get(i).getText());
                KoreanStockMarketCapTop20.add(lists.get(i).getText());

            }
        }catch (Exception e){
            log.error(e.getMessage());
        }


    }

    //기본 조회




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

}
