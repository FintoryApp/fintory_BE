package com.campuspick.fintory.domain.quiz.domain.entity;

import com.campuspick.fintory.domain.child.domain.entity.Childs;
import com.campuspick.fintory.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Table(name="daily_quizz_attempts")
public class DailyQuizzAttempts extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="remaining_attempts")
    private int remainingAttempts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="child_id")
    private Childs child;
}
