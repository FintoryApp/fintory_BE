package com.campuspick.fintory.domain.badge.domain.entity;

import com.campuspick.fintory.domain.child.domain.entity.Childs;
import com.campuspick.fintory.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name="user_badges")
public class UserBadges extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="child_id")
    private Childs child;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="badge_id")
    private Badges badge;
}
