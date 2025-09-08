package com.fintory.domain.point.model;

import com.fintory.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name="point_transaction")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointTransaction extends BaseEntity {

    private Long amount;

    @Enumerated(EnumType.STRING)
    private PointTransactionType type;

    @Enumerated(EnumType.STRING)
    private PointTransactionSource source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_id")
    private Point point;

}
