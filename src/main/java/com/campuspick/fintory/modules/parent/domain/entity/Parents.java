package com.campuspick.fintory.modules.parent.domain.entity;

import com.campuspick.fintory.global.entity.BaseTimeEntity;
import com.campuspick.fintory.modules.challenge.domain.entity.Challenges;
import com.campuspick.fintory.modules.child.domain.entity.DepositTransactions;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Table(name="parents")
public class Parents extends BaseTimeEntity {
    @Id
    private String id;

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

    //연관관계 설정
    @OneToMany(mappedBy="parent") //cascade 설정 일부러 안함
    private List<DepositTransactions> depositTransactions;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="parent")
    private List<Challenges> challenges;
}
