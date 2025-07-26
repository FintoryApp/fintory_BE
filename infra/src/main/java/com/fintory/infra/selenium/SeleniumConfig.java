package com.fintory.infra.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class SeleniumConfig {

    @Bean
    public WebDriver chromeDriver() {
        // WebDriverManager를 사용하여 ChromeDriver 자동 설정
        // 이 한 줄로 드라이버 다운로드 및 PATH 설정이 자동으로 처리됩니다.
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        // 헤드리스 모드: GUI 없이 백그라운드에서 브라우저 실행
        // 실제 서버 환경에서는 필수적으로 사용하는 옵션입니다.
        options.addArguments("--headless");
        // 샌드박스 비활성화: 일부 환경에서 필요한 옵션
        options.addArguments("--no-sandbox");
        // 공유 메모리 사용 비활성화: Docker 환경 등에서 발생할 수 있는 문제 해결
        options.addArguments("--disable-dev-shm-usage");
        // GPU 사용 비활성화: 성능 향상 및 안정성 확보 (특히 서버 환경)
        options.addArguments("--disable-gpu");
        // 웹 페이지 렌더링 시 이미지 로딩 비활성화 (선택 사항): 크롤링 속도 향상에 도움이 될 수 있으나,
        // 이미지가 중요한 크롤링이라면 제외해야 합니다.
        // options.addArguments("--blink-settings=imagesEnabled=false");

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
