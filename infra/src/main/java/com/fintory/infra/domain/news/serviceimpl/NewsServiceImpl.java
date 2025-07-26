package com.fintory.infra.domain.news.serviceimpl;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.news.dto.NewsResponse;
import com.fintory.domain.news.dto.NewsSummaryResponse;
import com.fintory.domain.news.model.News;
import com.fintory.domain.news.service.NewsService;
import com.fintory.infra.domain.news.repository.NewsRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;

    @Override
    @Transactional(readOnly = true)
    public NewsResponse getNews(Long id) {
        return newsRepository.findById(id)
                .map(NewsResponse::from)
                .orElseThrow(() -> new DomainException(DomainErrorCode.NEWS_NOT_FOUND));
    }
    
    @Override
    @Transactional(readOnly = true) // 읽기 전용 작업
    public List<NewsSummaryResponse> getNewsList() {
        List<News> latestArticles = newsRepository.findAllByOrderByPublishedAtDesc();
        if (latestArticles.isEmpty()) {
            return Collections.emptyList();
        }
        return latestArticles.stream()
                .map(NewsSummaryResponse::from)
                .collect(Collectors.toList());
    }

    
    
}