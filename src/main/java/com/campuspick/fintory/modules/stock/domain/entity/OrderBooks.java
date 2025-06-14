package com.campuspick.fintory.modules.stock.domain.entity;

import com.campuspick.fintory.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "order_books")
public class OrderBooks extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_id")
    private Long stockId;

    // 매도호가
    @Column(name = "sell_price_1", length = 4)
    private String sellPrice1;

    @Column(name = "sell_price_2", length = 4)
    private String sellPrice2;

    @Column(name = "sell_price_3", length = 4)
    private String sellPrice3;

    @Column(name = "sell_price_4", length = 4)
    private String sellPrice4;

    @Column(name = "sell_price_5", length = 4)
    private String sellPrice5;

    @Column(name = "sell_price_6", length = 4)
    private String sellPrice6;

    @Column(name = "sell_price_7", length = 4)
    private String sellPrice7;

    @Column(name = "sell_price_8", length = 4)
    private String sellPrice8;

    @Column(name = "sell_price_9", length = 4)
    private String sellPrice9;

    @Column(name = "sell_price_10", length = 4)
    private String sellPrice10;

    // 매수호가
    @Column(name = "buy_price_1", length = 4)
    private String buyPrice1;

    @Column(name = "buy_price_2", length = 4)
    private String buyPrice2;

    @Column(name = "buy_price_3", length = 4)
    private String buyPrice3;

    @Column(name = "buy_price_4", length = 4)
    private String buyPrice4;

    @Column(name = "buy_price_5", length = 4)
    private String buyPrice5;

    @Column(name = "buy_price_6", length = 4)
    private String buyPrice6;

    @Column(name = "buy_price_7", length = 4)
    private String buyPrice7;

    @Column(name = "buy_price_8", length = 4)
    private String buyPrice8;

    @Column(name = "buy_price_9", length = 4)
    private String buyPrice9;

    @Column(name = "buy_price_10", length = 4)
    private String buyPrice10;

    // 매도호가 잔량
    @Column(name = "sell_quantity_1", length = 8)
    private String sellQuantity1;

    @Column(name = "sell_quantity_2", length = 8)
    private String sellQuantity2;

    @Column(name = "sell_quantity_3", length = 8)
    private String sellQuantity3;

    @Column(name = "sell_quantity_4", length = 8)
    private String sellQuantity4;

    @Column(name = "sell_quantity_5", length = 8)
    private String sellQuantity5;

    @Column(name = "sell_quantity_6", length = 8)
    private String sellQuantity6;

    @Column(name = "sell_quantity_7", length = 8)
    private String sellQuantity7;

    @Column(name = "sell_quantity_8", length = 8)
    private String sellQuantity8;

    @Column(name = "sell_quantity_9", length = 8)
    private String sellQuantity9;

    @Column(name = "sell_quantity_10", length = 8)
    private String sellQuantity10;

    // 매수호가 잔량
    @Column(name = "buy_quantity_1", length = 8)
    private String buyQuantity1;

    @Column(name = "buy_quantity_2", length = 8)
    private String buyQuantity2;

    @Column(name = "buy_quantity_3", length = 8)
    private String buyQuantity3;

    @Column(name = "buy_quantity_4", length = 8)
    private String buyQuantity4;

    @Column(name = "buy_quantity_5", length = 8)
    private String buyQuantity5;

    @Column(name = "buy_quantity_6", length = 8)
    private String buyQuantity6;

    @Column(name = "buy_quantity_7", length = 8)
    private String buyQuantity7;

    @Column(name = "buy_quantity_8", length = 8)
    private String buyQuantity8;

    @Column(name = "buy_quantity_9", length = 8)
    private String buyQuantity9;

    @Column(name = "buy_quantity_10", length = 8)
    private String buyQuantity10;

    // 연관관계
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", insertable = false, updatable = false)
    private Stocks stock;
}