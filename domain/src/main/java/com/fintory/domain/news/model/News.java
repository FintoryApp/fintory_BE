package com.fintory.domain.news.model;

import com.fintory.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "news")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class News extends BaseEntity {

    private String title;

    private String content;

    private String publisher;

    @Column(name="thumbnail_image_url")
    private String thumbnailImageUrl;
}
