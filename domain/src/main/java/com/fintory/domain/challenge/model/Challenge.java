package com.fintory.domain.challenge.model;

import com.fintory.domain.child.model.Child;
import com.fintory.domain.common.BaseEntity;
import com.fintory.domain.parent.model.Parent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name="challenge")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Challenge extends BaseEntity {

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
