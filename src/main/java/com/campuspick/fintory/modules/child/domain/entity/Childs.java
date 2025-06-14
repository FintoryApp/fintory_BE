package com.campuspick.fintory.modules.child.domain.entity;

import com.campuspick.fintory.global.entity.BaseTimeEntity;
import com.campuspick.fintory.modules.alarms.domain.entity.Alarms;
import com.campuspick.fintory.modules.challenge.domain.entity.Challenges;
import com.campuspick.fintory.modules.news.domain.entity.Quizzes;
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
    private Account account;

    @OneToMany(cascade = CascadeType.ALL,mappedBy="child")
    private List<Points> point;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "child")
    private List<Quizzes> quizzes;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "child")
    private List<DailyQuizzAttempts> dailyQuizzAttempts;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="child")
    private List<UserBadges> userBadge;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="child")
    private List<Challenges> challenges;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="child")
    private List<Alarms> alarms;
}
