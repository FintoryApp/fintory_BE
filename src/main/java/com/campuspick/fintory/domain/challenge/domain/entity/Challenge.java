package com.campuspick.fintory.domain.challenge.domain.entity;

import com.campuspick.fintory.domain.child.domain.entity.Child;
import com.campuspick.fintory.domain.parent.domain.entity.Parent;
import com.campuspick.fintory.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name="challenges")
public class Challenge extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    private String reward;

    @Column(name="is_completed")
    private boolean isCompleted;

    @Column(name="is_rewarded")
    private boolean isRewarded;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_category_id")
    private ChallengeCategory challengeCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Parent parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id")
    private Child child;

}
