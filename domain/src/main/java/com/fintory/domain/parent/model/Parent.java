package com.fintory.domain.parent.model;

import com.fintory.domain.challenge.model.Challenge;
import com.fintory.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@Table(name="parents")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Parent extends BaseEntity {

    private String nickname;

    @Column(name="login_id")
    private String loginId;

    private String password;

    private String email;

    @Column(name="phone_number")
    private String phoneNumber;

    private String status; //혹시 여기도 boolean 타입이어야 하나요?

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="parent")
    private List<Challenge> challenges;
}


