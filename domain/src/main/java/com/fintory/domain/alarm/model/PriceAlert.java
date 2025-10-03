package com.fintory.domain.alarm.model;

import com.fintory.domain.child.model.Child;
import com.fintory.domain.common.BaseEntity;
import com.fintory.domain.stock.model.Stock;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name="price_alert")
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PriceAlert extends BaseEntity {

    @Column(name="target_price",precision = 15, scale=3)
    private BigDecimal targetPrice;

    @ManyToOne
    @JoinColumn(name="child_id")
    private Child child;

    @ManyToOne
    @JoinColumn(name="stock_id")
    private Stock stock;


    //REVIEW 감시가는 단순히 특정 가격이 되면 알림 보내기 용도 -> 굳이 삭제된 데이터를 보관할 이유가 없음
    // 과거 감시가 데이터가 의미없는 상황에서 소프트 딜리트는 쓸모없는 데이터가 계속 쌓이는 것으로 판단. 혹시 필요하다면 소프트 딜리트 추가하겠음
}
