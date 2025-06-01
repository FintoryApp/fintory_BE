package com.campuspick.fintory.modules.news.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
}
