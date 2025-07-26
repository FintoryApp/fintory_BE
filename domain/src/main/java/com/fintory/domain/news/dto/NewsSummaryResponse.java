package com.fintory.domain.news.dto;

import com.fintory.domain.news.model.News;

public record NewsSummaryResponse(

        String title,
        String content,
        String publishedAt,
        String publisher,
        String thumbnailUrl

) {

    public static NewsSummaryResponse from(News article) {
        return new NewsSummaryResponse(
                article.getTitle(),
                article.getContents(),
                article.getPublishedAt(),
                article.getPublisher(),
                article.getImageUrl()
        );
    }
}
