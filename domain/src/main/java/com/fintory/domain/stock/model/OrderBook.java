package com.fintory.domain.stock.model;

import com.fintory.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Builder
@AllArgsConstructor
@Table(name = "order_book")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderBook extends BaseEntity {

    // 매도호가
    @Column(name = "sell_price_1", length = 4)
    private BigDecimal sellPrice1;

    @Column(name = "sell_price_2", length = 4)
    private BigDecimal sellPrice2;

    @Column(name = "sell_price_3", length = 4)
    private BigDecimal sellPrice3;

    @Column(name = "sell_price_4", length = 4)
    private BigDecimal sellPrice4;

    @Column(name = "sell_price_5", length = 4)
    private BigDecimal sellPrice5;

    @Column(name = "sell_price_6", length = 4)
    private BigDecimal sellPrice6;

    @Column(name = "sell_price_7", length = 4)
    private BigDecimal sellPrice7;

    @Column(name = "sell_price_8", length = 4)
    private BigDecimal sellPrice8;

    @Column(name = "sell_price_9", length = 4)
    private BigDecimal sellPrice9;

    @Column(name = "sell_price_10", length = 4)
    private BigDecimal sellPrice10;

    // 매수호가
    @Column(name = "buy_price_1", length = 4)
    private BigDecimal buyPrice1;

    @Column(name = "buy_price_2", length = 4)
    private BigDecimal buyPrice2;

    @Column(name = "buy_price_3", length = 4)
    private BigDecimal buyPrice3;

    @Column(name = "buy_price_4", length = 4)
    private BigDecimal buyPrice4;

    @Column(name = "buy_price_5", length = 4)
    private BigDecimal buyPrice5;

    @Column(name = "buy_price_6", length = 4)
    private BigDecimal buyPrice6;

    @Column(name = "buy_price_7", length = 4)
    private BigDecimal buyPrice7;

    @Column(name = "buy_price_8", length = 4)
    private BigDecimal buyPrice8;

    @Column(name = "buy_price_9", length = 4)
    private BigDecimal buyPrice9;

    @Column(name = "buy_price_10", length = 4)
    private BigDecimal buyPrice10;

    // 매도호가 잔량
    @Column(name = "sell_quantity_1", length = 8)
    private Long sellQuantity1;

    @Column(name = "sell_quantity_2", length = 8)
    private Long sellQuantity2;

    @Column(name = "sell_quantity_3", length = 8)
    private Long sellQuantity3;

    @Column(name = "sell_quantity_4", length = 8)
    private Long sellQuantity4;

    @Column(name = "sell_quantity_5", length = 8)
    private Long sellQuantity5;

    @Column(name = "sell_quantity_6", length = 8)
    private Long sellQuantity6;

    @Column(name = "sell_quantity_7", length = 8)
    private Long sellQuantity7;

    @Column(name = "sell_quantity_8", length = 8)
    private Long sellQuantity8;

    @Column(name = "sell_quantity_9", length = 8)
    private Long sellQuantity9;

    @Column(name = "sell_quantity_10", length = 8)
    private Long sellQuantity10;

    // 매수호가 잔량
    @Column(name = "buy_quantity_1", length = 8)
    private Long buyQuantity1;

    @Column(name = "buy_quantity_2", length = 8)
    private Long buyQuantity2;

    @Column(name = "buy_quantity_3", length = 8)
    private Long buyQuantity3;

    @Column(name = "buy_quantity_4", length = 8)
    private Long buyQuantity4;

    @Column(name = "buy_quantity_5", length = 8)
    private Long buyQuantity5;

    @Column(name = "buy_quantity_6", length = 8)
    private Long buyQuantity6;

    @Column(name = "buy_quantity_7", length = 8)
    private Long buyQuantity7;

    @Column(name = "buy_quantity_8", length = 8)
    private Long buyQuantity8;

    @Column(name = "buy_quantity_9", length = 8)
    private Long buyQuantity9;

    @Column(name = "buy_quantity_10", length = 8)
    private Long buyQuantity10;

    // 연관관계
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;
}