package com.campuspick.fintory.domain.child.domain.entity;

import com.campuspick.fintory.domain.attendance.domain.entity.VisitLogs;
import com.campuspick.fintory.domain.point.domain.entity.Points;
import com.campuspick.fintory.domain.badge.domain.entity.UserBadges;
import com.campuspick.fintory.domain.quiz.domain.entity.DailyQuizzAttempts;
import com.campuspick.fintory.domain.account.domain.entity.Accounts;
import com.campuspick.fintory.domain.quiz.domain.entity.MyQuizzes;
import com.campuspick.fintory.global.entity.BaseTimeEntity;
import com.campuspick.fintory.domain.alarms.domain.entity.Alarms;
import com.campuspick.fintory.domain.challenge.domain.entity.Challenges;
import com.campuspick.fintory.domain.quiz.domain.entity.Quizzes;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Table(name="childs")
public class Childs  extends BaseTimeEntity {


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
    private Accounts account;

    @OneToOne(cascade = CascadeType.ALL, mappedBy="child")
    private ParentChildMappings parentChildMapping;

    @OneToMany(cascade = CascadeType.ALL,mappedBy="child")
    private List<Points> point;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "child")
    private List<MyQuizzes> myQuizzes;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "child")
    private List<DailyQuizzAttempts> dailyQuizzAttempts;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="child")
    private List<UserBadges> userBadge;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="child")
    private List<Challenges> challenges;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="child")
    private List<Alarms> alarms;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="child")
    private List<VisitLogs> visitLogs;
}
