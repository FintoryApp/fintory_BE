package com.campuspick.fintory.domain.child.domain.entity;

import com.campuspick.fintory.domain.account.domain.entity.Account;
import com.campuspick.fintory.domain.attendance.domain.entity.VisitLog;
import com.campuspick.fintory.domain.point.domain.entity.Point;
import com.campuspick.fintory.domain.badge.domain.entity.UserBadge;
import com.campuspick.fintory.domain.quiz.domain.entity.DailyQuizzAttempt;
import com.campuspick.fintory.domain.quiz.domain.entity.MyQuizz;
import com.campuspick.fintory.global.entity.BaseTimeEntity;
import com.campuspick.fintory.domain.alarms.domain.entity.Alarm;
import com.campuspick.fintory.domain.challenge.domain.entity.Challenge;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Table(name="childs")
public class Child extends BaseTimeEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    private List<MyQuizz> myQuizzes;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "child")
    private List<DailyQuizzAttempt> dailyQuizzAttempts;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="child")
    private List<UserBadge> userBadge;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="child")
    private List<Challenge> challenges;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="child")
    private List<Alarm> alarms;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="child")
    private List<VisitLog> visitLogs;
}
