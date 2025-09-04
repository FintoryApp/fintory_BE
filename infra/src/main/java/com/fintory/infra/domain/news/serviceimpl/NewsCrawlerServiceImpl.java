package com.fintory.infra.domain.news.serviceimpl;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.news.model.News;
import com.fintory.domain.news.service.NewsCrawlerService;
import com.fintory.infra.domain.news.repository.NewsRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class NewsCrawlerServiceImpl implements NewsCrawlerService {

    private final NewsRepository newsRepository;

    @Override
    @Transactional
    public void crawlAndSaveLatestNews() {

        log.info("[Crawler] 자정 뉴스 크롤링 및 저장 시작...");
        String mainPageUrl = "https://www.chosun.com/kid/kid_literacy/";

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");

        WebDriver driver = null;

        try{
            driver = new RemoteWebDriver(new URL("http://selenium:4444/wd/hub"), options);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            List<String> latestNewsLinks = getLatestNewsLinks(driver, wait, mainPageUrl);

            List<News> crawledArticles = new ArrayList<>();
            for (String link : latestNewsLinks) {
                News article = Optional.ofNullable(crawlArticleDetail(driver, wait, link))
                        .orElseThrow(() -> new DomainException(DomainErrorCode.NEWS_CRAWLING_FAILED));
                crawledArticles.add(article);
            }

            newsRepository.deleteAllNewsArticles(); // 기존 뉴스 모두 삭제
            newsRepository.saveAll(crawledArticles); // 크롤링된 모든 기사 저장 -> 항상 최신 3개 기사 덮어쓰기로 저장

        } catch (Exception e) {
            throw new DomainException(DomainErrorCode.NEWS_CRAWLING_FAILED);
        } finally {
            if (driver != null) {
                driver.quit();
                log.info("[Crawler] 뉴스 크롤링 후 세션 종료");
            }
        }
    }

    // 최신 3개 기사 url 반환
    private List<String> getLatestNewsLinks(WebDriver driver, WebDriverWait wait, String url) {
        driver.get(url);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.story-feed")));
        Document doc = Jsoup.parse(driver.getPageSource());
        Elements articleElements = doc.select("a.story-card__headline[href]\n");
        if (articleElements.isEmpty()) { throw new DomainException(DomainErrorCode.NEWS_LINK_GET_FAILED); }

        String baseUrl = "https://www.chosun.com";
        return articleElements.stream()
                .map(e -> e.attr("href"))
                .filter(href -> href != null && !href.trim().isEmpty())
                .map(href -> href.startsWith("/") ? baseUrl + href : href)
                .distinct()
                .limit(3)
                .collect(Collectors.toList());
    }

    private News crawlArticleDetail(WebDriver driver, WebDriverWait wait, String articleUrl) {
        driver.get(articleUrl);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.layout-main")));
        Document doc = Jsoup.parse(driver.getPageSource());

        String title = "";
        String publisher = "";
        String publishedAt = "";
        List<String> contents = new ArrayList<>();
        String imageUrl = "";
        
        // 제목 추출
        Element titleElement = doc.selectFirst("h1.article-header__headline > span");
        if (titleElement != null) {
            title = titleElement.text().trim();
        }

        // 기자 추출
        Element publisherElement = doc.selectFirst("a.article-byline__author");
        if (publisherElement != null) {
            publisher = publisherElement.text().trim();
        }

        // 출간일 추출
        Element publishedAtElement = doc.selectFirst("span.dateBox span.inputDate");
        if (publishedAtElement != null) {
            publishedAt = publishedAtElement.text().trim();
            log.info("published_at: {}", publishedAt);
        }
        
        // 내용 추출:
        Elements contentParagraphs = doc.select("p.article-body__content.article-body__content-text.text--black.text.font--size-sm-18.font--size-md-18.font--primary");
        for (Element p : contentParagraphs) {
            String text = p.text().trim();
            if (!text.isEmpty()) {
                contents.add(text);
            }
        }

        // 첫 번째 이미지만 추출
        Element figureElement = doc.selectFirst("figure.article-body__content.article-body__content-image.visual__image.visual__image--cover");
        if (figureElement != null) {
            Element imgElement = figureElement.selectFirst("div.lazyload-wrapper img");
            if (imgElement != null) {
                String src = imgElement.attr("src");
                if (src.startsWith("https://")) {
                    imageUrl = src;
                }
            }
        }

        return News.builder()
                .title(title)
                .contents(String.join("\n\n", contents))
                .imageUrl(imageUrl)
                .publisher(publisher)
                .publishedAt(publishedAt)
                .build();
    }
}