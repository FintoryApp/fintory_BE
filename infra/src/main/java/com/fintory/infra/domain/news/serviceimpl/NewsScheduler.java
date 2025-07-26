package com.fintory.infra.domain.news.serviceimpl;

import com.fintory.domain.news.service.NewsCrawlerService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class NewsScheduler {

    private final NewsCrawlerService newsCrawlerService;

    // 매일 자정 (새벽 0시 0분 0초)에 실행
    @Scheduled(cron = "0 0 0 * * *")
    public void runDailyNewsCrawling() {
        newsCrawlerService.crawlAndSaveLatestNews();
    }
}
