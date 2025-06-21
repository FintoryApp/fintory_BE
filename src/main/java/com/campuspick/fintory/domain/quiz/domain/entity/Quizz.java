package com.campuspick.fintory.domain.quiz.domain.entity;

import com.campuspick.fintory.domain.term.domain.entity.EconomicTerm;
import com.campuspick.fintory.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name="quizzes")
public class Quizz extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;

    private String answer;

    private String explanation;

    //현재는 term - quiz가 일대일 관계로 변경했으나, 수정 자유
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="term_id")
    private EconomicTerm term;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "quiz")
    private List<MyQuizz> myQuizzes;
}
