package com.fintory.child.domain.news.controller;

import com.fintory.common.api.ApiResponse;
import com.fintory.domain.news.dto.NewsResponse;
import com.fintory.domain.news.dto.NewsSummaryResponse;
import com.fintory.domain.news.service.NewsCrawlerService;
import com.fintory.domain.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
public class NewsControllerImpl implements NewsController{

    private final NewsService newsService;
    private final NewsCrawlerService newsCrawlerService;

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NewsResponse>> getNews(
            @PathVariable Long id
    ){
        NewsResponse news = newsService.getNews(id);
        return ResponseEntity.ok(ApiResponse.ok(news));
    }

    @Override
    @GetMapping("/getNewsList")
    public ResponseEntity<ApiResponse<List<NewsSummaryResponse>>> getNewsList(){
        List<NewsSummaryResponse> list = newsService.getNewsList();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @Override
    @PostMapping("/crawl-test")
    public ResponseEntity<ApiResponse<Void>> triggerCrawling() {
        newsCrawlerService.crawlAndSaveLatestNews();
        return ResponseEntity.ok(ApiResponse.okWithNoData(null));
    }

}
