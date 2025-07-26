package com.fintory.domain.news.service;

import com.fintory.domain.news.dto.NewsResponse;
import com.fintory.domain.news.dto.NewsSummaryResponse;

import java.util.List;

public interface NewsService {

    NewsResponse getNews(Long id);

    List<NewsSummaryResponse> getNewsList();
}
