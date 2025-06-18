package com.campuspick.fintory.domain.consulting.domain.entity;

import com.campuspick.fintory.global.entity.BaseTimeEntity;
import com.campuspick.fintory.domain.account.domain.entity.Accounts;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Getter
@Table(name="reports")
public class Reports extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String performance;

    @Column(name="risk_type")
    private String riskType;

    private String advice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="account_id")
    private Accounts account;

}
