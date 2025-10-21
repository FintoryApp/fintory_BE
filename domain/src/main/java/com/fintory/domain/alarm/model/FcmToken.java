package com.fintory.domain.alarm.model;

import com.fintory.domain.child.model.Child;
import com.fintory.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
/*
*
* 한 명의 사용자가 여러 기기에서 로그인하면 모든 기기에 알림을 보내줘야 함
* 따라서 FcmToken을 child에서 list 형식으로 가짐
*
* */
public class FcmToken extends BaseEntity {

    @Column(nullable=false)
    private String token;

    @ManyToOne
    @JoinColumn(name="child_id")
    private Child child;

}
