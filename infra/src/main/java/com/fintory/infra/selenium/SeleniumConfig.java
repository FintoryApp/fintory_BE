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
    public WebDriver chromeDriver() {
        ChromeOptions options = new ChromeOptions();
        options.setCapability("browserName", "chrome");

        RemoteWebDriver driver = null;
        try {
            driver = new RemoteWebDriver(
                    new URL("http://selenium:4444/wd/hub"),
                    options
            );
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Selenium URL", e);
        }

        return driver;
    }

    @Bean
    public WebDriverWait webDriverWait(WebDriver webDriver) {
        return new WebDriverWait(
                webDriver,
                Duration.ofSeconds(10)  // 기본 타임아웃 시간
        );
    }

}
