package com.campuspick.fintory.domain.term.domain.entity;

import com.campuspick.fintory.domain.quiz.domain.entity.Quizzes;
import com.campuspick.fintory.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name="economic_terms")
public class EconomicTerms extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    private String title;

    @OneToOne(mappedBy = "term")
    private Quizzes quizz;
}
