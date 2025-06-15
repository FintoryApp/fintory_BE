package com.campuspick.fintory.domain.news.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Table(name = "news")
@Getter
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    private String publisher;

    @Column(name="thumbnail_image_url")
    private String thumbnailImageUrl;
}
