package com.campuspick.fintory.modules.child.domain.entity;


import com.campuspick.fintory.global.entity.BaseTimeEntity;
import com.campuspick.fintory.modules.news.domain.entity.Quizzes;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name="my_quizzes")
public class MyQuizzes extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="child_id")
    private Childs child;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="quizz_id")
    private Quizzes quiz;

    @Column(name="is_correct")
    private boolean isCorrect;

    @Column(name="my_answer")
    private String myAnswer;

}
