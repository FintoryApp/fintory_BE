package com.fintory.domain.consulting.model;

import com.fintory.domain.account.model.Account;
import com.fintory.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@Table(name="report")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseEntity {

    private String performance;

    @Column(name="risk_type")
    private String riskType;

    private String advice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="account_id")
    private Account account;

}
