package com.campuspick.fintory.domain.point.domain.entity;

import com.campuspick.fintory.domain.child.domain.entity.Childs;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Table(name="points")
public class Points{
    // 이렇게 되면 포인트 테이블이 아닌 포인트 history 테이블이 됨. -> amount를 계산하려면 SUM(amount)가 된다는 사실 기억

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int amount;

    private String category;

    @Column(name="earned_at")
    private LocalDateTime earnedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="child_id")
    private Childs child;
}
