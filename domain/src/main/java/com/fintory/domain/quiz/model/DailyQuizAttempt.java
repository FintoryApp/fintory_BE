package com.fintory.domain.quiz.model;

import com.fintory.domain.child.model.Child;
import com.fintory.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name="daily_quizz_attempt")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyQuizAttempt extends BaseEntity {

    @Column(name="remaining_attempts")
    private int remainingAttempts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="child_id")
    private Child child;
}
