package com.fintory.domain.mapping;


import com.fintory.domain.child.model.Child;
import com.fintory.domain.common.BaseEntity;
import com.fintory.domain.quiz.model.Quiz;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name="my_quiz")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MyQuiz extends BaseEntity {

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
    private Quiz quiz;
}
