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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class NewsCrawlerServiceImpl implements NewsCrawlerService {

    private final WebDriver webDriver;
    private final WebDriverWait webDriverWait;
    private final NewsRepository newsRepository;

    @Override
    @Transactional
    public void crawlAndSaveLatestNews() {

        log.info("[Crawler] 자정 뉴스 크롤링 및 저장 시작...");
        String mainPageUrl = "https://www.chosun.com/kid/kid_economy/kid_honeybee/";

        // 최신 뉴스 3개 링크 추출
        List<String> latestNewsLinks = getLatestNewsLinks(mainPageUrl);

        // 최신 뉴스 3개 크롤링
        List<News> crawledArticles = latestNewsLinks.stream()
                .map(link -> Optional.ofNullable(crawlArticleDetail(link))
                        .orElseThrow(() -> new DomainException(DomainErrorCode.NEWS_CRAWLING_FAILED)))
                .collect(Collectors.toList());

        newsRepository.deleteAllNewsArticles(); // 기존 뉴스 모두 삭제
        newsRepository.saveAll(crawledArticles); // 크롤링된 모든 기사 저장 -> 항상 최신 3개 기사 덮어쓰기로 저장
    }

    // 최신 3개 기사 url 반환
    private List<String> getLatestNewsLinks(String url) {
        webDriver.get(url);
        webDriverWait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.story-feed")));
        Document doc = Jsoup.parse(webDriver.getPageSource());
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

    private News crawlArticleDetail(String articleUrl) {

        webDriver.get(articleUrl);
        webDriverWait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.layout-main")));
        Document doc = Jsoup.parse(webDriver.getPageSource());

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