package com.campuspick.fintory.domain.badge.domain.entity;


import com.campuspick.fintory.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name="badges")
public class Badges extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @Column(name="icon_url")
    private String iconUrl;

    //연관관계 설정
    @OneToMany(mappedBy="badge")
    private List<UserBadges> userBadge;
}
