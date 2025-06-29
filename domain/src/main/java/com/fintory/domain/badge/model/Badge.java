package com.fintory.domain.badge.model;

import com.fintory.domain.common.BaseEntity;
import com.fintory.domain.mapping.UserBadge;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@Table(name="badge")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Badge extends BaseEntity {

    private String name;

    private String description;

    @Column(name="icon_url")
    private String iconUrl;

    //연관관계 설정
    @OneToMany(mappedBy="badge")
    private List<UserBadge> userBadge;
}
