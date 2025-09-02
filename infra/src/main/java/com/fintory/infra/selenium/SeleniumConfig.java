package com.fintory.infra.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

@Configuration
public class SeleniumConfig {

    @Bean
    public WebDriver chromeDriver() {

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");

        String userDataDir = "/tmp/chrome-user-data-" + UUID.randomUUID();
        new File(userDataDir).mkdirs();
        options.addArguments("--user-data-dir=" + userDataDir);

        return new ChromeDriver(options);
    }

    @Bean
    public WebDriverWait webDriverWait(WebDriver webDriver) {
        return new WebDriverWait(
                webDriver,
                Duration.ofSeconds(10)  // 기본 타임아웃 시간
        );
    }

}
