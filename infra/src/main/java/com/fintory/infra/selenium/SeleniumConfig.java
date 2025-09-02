package com.fintory.infra.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

@Configuration
public class SeleniumConfig {

    @Bean
    public WebDriver chromeDriver() throws MalformedURLException {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");

        // RemoteWebDriver 연결 (selenium 서비스와 연결)
        return new RemoteWebDriver(
                new URL("http://selenium:4444/wd/hub"),  // 도커 네트워크 내 서비스 이름 사용
                options
        );
    }

    @Bean
    public WebDriverWait webDriverWait(WebDriver webDriver) {
        return new WebDriverWait(
                webDriver,
                Duration.ofSeconds(10)  // 기본 타임아웃 시간
        );
    }

}
