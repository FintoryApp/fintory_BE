package com.campuspick.fintory.domain.stock.domain.entity;

import com.campuspick.fintory.global.entity.BaseTimeEntity;
import com.campuspick.fintory.domain.portfolio.domain.entity.OwnedStock;
import com.campuspick.fintory.domain.portfolio.domain.entity.StockTransaction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name="stocks")
public class Stock extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

    private String ticker;

    private String code;

    @Column(name="market_name")
    private String marketName;

    @Column(name="currency_name")
    private String currencyName;

    private String name;

    private String eng_name;

    private String category;

    @Column(name="sell_unit")
    private int sellUnit;

    @Column(name="buy_unit")
    private int buyUnit;

    //연관관계 설정
    @OneToOne(cascade = CascadeType.ALL,mappedBy="stock")
    private LiveStockPrice liveStockPrice;

    @OneToMany(cascade = CascadeType.ALL,mappedBy="stock")
    private List<StockTransaction> stockTransaction;

    @OneToMany(cascade=CascadeType.ALL, mappedBy="stock")
    private List<StockPriceHistory> stockPriceHistories;

    @OneToMany(cascade=CascadeType.ALL, mappedBy="stock")
    private List<OwnedStock> ownedStocks;

    @OneToOne(cascade=CascadeType.ALL, mappedBy="stock")
    private OrderBook orderBook;
}
