package com.fintory.domain.point.model;

import com.fintory.domain.child.model.Child;
import com.fintory.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name="point")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point extends BaseEntity {
    // 이렇게 되면 포인트 테이블이 아닌 포인트 history 테이블이 됨. -> amount를 계산하려면 SUM(amount)가 된다는 사실 기억

    private int amount;

    private String category;

    @Column(name="earned_at")
    private LocalDateTime earnedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="child_id")
    private Child child;
}
