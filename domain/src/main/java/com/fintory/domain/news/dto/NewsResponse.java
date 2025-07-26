package com.fintory.domain.news.dto;

import com.fintory.domain.news.model.News;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record NewsResponse (
        String title,
        List<String> contents,
        String images,
        String publishedAt,
        String publisher
) {
    // 줄바꿈 2개 기준으로 문단 파싱
    public static NewsResponse from(News article){
        List<String> contentsList = Optional.ofNullable(article.getContents())
                .filter(s -> !s.isEmpty())
                .map(s -> Arrays.asList(s.split("\n\n")))
                .orElse(Collections.emptyList());

        return new NewsResponse(
                article.getTitle(),
                contentsList,
                article.getImageUrl(),
                article.getPublishedAt(),
                article.getPublisher()
        );
    }
}
