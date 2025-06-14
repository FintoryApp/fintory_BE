package com.campuspick.fintory.modules.news.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Table(name = "news")
@Getter
public class News {

    @Id
    private Long id;

    private String title;

    private String content;

    private String publisher;

    @Column(name="thumbnail_image_url")
    private String thumbnailImageUrl;

    @OneToOne(mappedBy = "news")
    private Quizzes quiz;
}
