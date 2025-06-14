package com.campuspick.fintory.modules.consulting.domain.entity;

import com.campuspick.fintory.global.entity.BaseTimeEntity;
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

    @ManyToOne
    @JoinColumn(name="account_id")
    private Account account;

}
