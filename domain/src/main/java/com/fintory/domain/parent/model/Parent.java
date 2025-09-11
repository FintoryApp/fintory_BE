package com.fintory.domain.parent.model;

import com.fintory.domain.child.model.LoginType;
import com.fintory.domain.child.model.Status;
import com.fintory.domain.common.BaseEntity;
import com.fintory.domain.common.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name="parents")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Parent extends BaseEntity {

    @Column(length = 20)
    private String nickname;

    @Column(unique = true)
    private String email;

    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name="social_id", unique = true)
    private String socialId;

    @Column(name="social_type")
    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    @Enumerated(EnumType.STRING)
    private Status status;

    public static Parent createWithIdPw(String nickname, String email, String password, Role role, Status status) {
        Parent parent = new Parent();
        parent.nickname = nickname;
        parent.email = email;
        parent.password = password;
        parent.role = role;
        parent.loginType = LoginType.EMAIL;
        parent.status = status;
        return parent;
    }

    public static Parent createWithSocial(String nickname, String email, String socialId, LoginType loginType, Role role, Status status) {
        Parent parent = new Parent();
        parent.nickname = nickname;
        parent.email = email;
        parent.socialId = socialId;
        parent.loginType = loginType;
        parent.role = role;
        parent.status = status;
        return parent;
    }

}


