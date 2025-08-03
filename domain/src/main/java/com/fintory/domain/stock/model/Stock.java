package com.fintory.domain.stock.model;

import com.fintory.domain.common.BaseEntity;
import com.fintory.domain.portfolio.model.OwnedStock;
import com.fintory.domain.portfolio.model.StockTransaction;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@Table(name="stock")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock extends BaseEntity {

    private String ticker;

    private String code;

    @Column(name="market_name")
    private String marketName;

    @Column(name="currency_name")
    private String currencyName;

    private String name;

    private String eng_name;

    private String category;

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
