package com.campuspick.fintory.modules.news.domain.entity;

import com.campuspick.fintory.global.entity.BaseTimeEntity;
import com.campuspick.fintory.modules.child.domain.entity.MyQuizzes;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name="quizzes")
public class Quizzes extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;

    private String answer;

    private String explanation;

    @OneToOne
    @JoinColumn(name="news_id")
    private News news;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "quizz")
    private List<MyQuizzes> quizzes;
}
