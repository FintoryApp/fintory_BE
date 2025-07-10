package com.fintory.domain.consulting.model;

import com.fintory.domain.account.model.Account;
import com.fintory.domain.child.model.Child;
import com.fintory.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.YearMonth;


@Entity
@Getter
@Table(name="report")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseEntity {

    // private String performance;

    @Column(name="risk_type")
    private String riskType;

    private String advice;

    private String reportMonth;

    @Column(columnDefinition = "TEXT")
    private String reportJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="account_id")
    private Account account;

    //명시적으로 child 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="child_id")
    private Child child;


}
