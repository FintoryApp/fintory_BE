package com.fintory.domain.child.model;

import com.fintory.domain.account.model.Account;
import com.fintory.domain.alarm.model.Alarm;
import com.fintory.domain.attendence.model.AttendanceLog;
import com.fintory.domain.challenge.model.Challenge;
import com.fintory.domain.common.BaseEntity;
import com.fintory.domain.common.Role;
import com.fintory.domain.common.User;
import com.fintory.domain.consulting.model.Report;
import com.fintory.domain.mapping.ParentChildMapping;
import com.fintory.domain.point.model.PointWallet;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Table(name="child")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Child extends BaseEntity implements User {

    @Column(length = 20)
    private String nickname;

    @Column(unique = true)
    private String email;

    @Column
    private String password;

    // 시큐리티에서 자격 검사할때 필요해서 추가(인증에는 필요없지만, 인가에 필요)
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    //social login column
    @Column(name="social_id", unique = true)
    private String socialId;

    @Column(name="social_type")
    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    @Enumerated(EnumType.STRING)
    private Status status;

    // id&pw 생성자
    @Builder(builderMethodName = "idPwBuilder")
    public Child(String nickname, String email, String password, Role role, LoginType loginType, Status status) {
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.role = role;
        this.loginType = loginType;
        this.status = status;
    }
    // social login 생성자
    public Child(String nickname, String email, String socialId, LoginType loginType, Role role, Status status) {
        this.nickname = nickname;
        this.email = email;
        this.socialId = socialId;
        this.loginType = loginType;
        this.role = role;
        this.status = status;
    }

    public void updateStatus(Status status) {
        this.status = status;
    }


    // n:m
    @OneToOne(cascade = CascadeType.ALL, mappedBy="child")
    private ParentChildMapping parentChildMapping;

    // 1:1
    @OneToOne(cascade = CascadeType.ALL, mappedBy="child")
    private Account account;

    @OneToOne(cascade = CascadeType.ALL,mappedBy="child")
    private PointWallet pointWallet;

    // 1:n
    @OneToMany(cascade = CascadeType.ALL, mappedBy="child")
    private List<Challenge> challenges;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="child")
    private List<Alarm> alarms;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="child")
    private List<AttendanceLog> visitLogs;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "child")
    private List<Report> reports;



}
