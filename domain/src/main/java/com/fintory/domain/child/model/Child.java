package com.fintory.domain.child.model;

import com.fintory.domain.account.model.Account;
import com.fintory.domain.alarm.model.Alarm;
import com.fintory.domain.attendence.model.VisitLog;
import com.fintory.domain.challenge.model.Challenge;
import com.fintory.domain.common.BaseEntity;
import com.fintory.domain.common.Role;
import com.fintory.domain.mapping.MyQuiz;
import com.fintory.domain.mapping.ParentChildMapping;
import com.fintory.domain.point.model.Point;
import com.fintory.domain.quiz.model.DailyQuizAttempt;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Table(name="child")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Child extends BaseEntity {

    @Column(length = 20)
    private String nickname;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    // 시큐리티에서 자격 검사할때 필요해서 추가(인증에는 필요없지만, 인가에 필요)
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    //social login column
    @Column(name="social_id")
    private String socialId;

    @Column(name="social_type")
    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    @Enumerated(EnumType.STRING)
    private Status status;

    // id&pw 생성자
    @Builder
    public Child(String nickname, String email, String password, Role role, Status status) {
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
    }
    // social 생성자
    @Builder
    public Child(String nickname, String email, String socialId, SocialType socialType, Status status) {
        this.nickname = nickname;
        this.email = email;
        this.socialId = socialId;
        this.socialType = socialType;
        this.status = status;
    }
    // id&pw 방식으로 계정 생성후 social 로그인 추가하는 setter method
    public void updateSocialInfo (String socialId, SocialType socialType) {
        this.socialId = socialId;
        this.socialType = socialType;
    }

    public void updateStatus(Status status) {
        this.status = status;
    }


    // n:m
    @OneToOne(cascade = CascadeType.ALL, mappedBy="child")
    private ParentChildMapping parentChildMapping;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "child")
    private List<MyQuiz> myQuizs;

    // 1:1
    @OneToOne(cascade = CascadeType.ALL, mappedBy="child")
    private Account account;

    // 1:n
    @OneToMany(cascade = CascadeType.ALL,mappedBy="child")
    private List<Point> point;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "child")
    private List<DailyQuizAttempt> dailyQuizzAttempts;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="child")
    private List<Challenge> challenges;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="child")
    private List<Alarm> alarms;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="child")
    private List<VisitLog> visitLogs;

}
