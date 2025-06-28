package com.fintory.domain.child.model;

import com.fintory.domain.account.model.Account;
import com.fintory.domain.alarm.model.Alarm;
import com.fintory.domain.attendence.model.VisitLog;
import com.fintory.domain.mapping.MyQuiz;
import com.fintory.domain.mapping.UserBadge;
import com.fintory.domain.challenge.model.Challenge;
import com.fintory.domain.common.BaseEntity;
import com.fintory.domain.mapping.ParentChildMapping;
import com.fintory.domain.point.model.Point;
import com.fintory.domain.quiz.model.DailyQuizAttempt;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@Table(name="child")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Child extends BaseEntity {

    private String nickname;

    private int age;

    @Column(name="login_id")
    private String loginId;

    private String password;

    private String email;

    private boolean status;

    //연관관계
    @OneToOne(cascade = CascadeType.ALL, mappedBy="child")
    private Account account;

    @OneToOne(cascade = CascadeType.ALL, mappedBy="child")
    private ParentChildMapping parentChildMapping;

    @OneToMany(cascade = CascadeType.ALL,mappedBy="child")
    private List<Point> point;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "child")
    private List<MyQuiz> myQuizs;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "child")
    private List<DailyQuizAttempt> dailyQuizzAttempts;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="child")
    private List<UserBadge> userBadge;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="child")
    private List<Challenge> challenges;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="child")
    private List<Alarm> alarms;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="child")
    private List<VisitLog> visitLogs;
}
