package com.campuspick.fintory.domain.quiz.domain.entity;


import com.campuspick.fintory.domain.child.domain.entity.Child;
import com.campuspick.fintory.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name="my_quizzes")
public class MyQuizz extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="is_correct")
    private boolean isCorrect;

    @Column(name="my_answer")
    private String myAnswer;

    @Column(name="answered_at")
    private LocalDateTime answeredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="child_id")
    private Child child;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="quizz_id")
    private Quizz quiz;
}
