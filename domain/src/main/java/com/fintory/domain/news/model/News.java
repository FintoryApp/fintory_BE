package com.fintory.domain.news.model;

import com.fintory.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "news")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class News extends BaseEntity {

    private String title;

    // 한 기사의 여러 문단을 공백을 구분으로 하는 긴 문자열로 한번에 저장
    @Column(columnDefinition = "TEXT")
    private String contents;

    @Column(name="image_url")
    private String imageUrl;

    private String publisher;

    @Column(name="published_at")
    private String publishedAt;

    @Builder
    private News(String title, String contents, String imageUrl, String publisher, String publishedAt) {
        this.title = title;
        this.contents = contents;
        this.imageUrl = imageUrl;
        this.publisher = publisher;
        this.publishedAt = publishedAt;
    }
}
