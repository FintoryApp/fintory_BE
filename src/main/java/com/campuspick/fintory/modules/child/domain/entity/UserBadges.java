package com.campuspick.fintory.modules.child.domain.entity;

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

    @ManyToOne
    @JoinColumn(name="child_id")
    private Childs child;

    @ManyToOne
    @JoinColumn(name="badge_id")
    private Badges badge;

}
