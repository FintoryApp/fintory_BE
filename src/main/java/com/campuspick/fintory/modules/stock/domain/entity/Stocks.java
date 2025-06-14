package com.campuspick.fintory.modules.stock.domain.entity;

import com.campuspick.fintory.global.entity.BaseTimeEntity;
import com.campuspick.fintory.modules.child.domain.entity.OwnedStocks;
import com.campuspick.fintory.modules.child.domain.entity.StockTransactions;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name="stocks")
public class Stocks extends BaseTimeEntity {

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
    private LiveStockPrices liveStockPrice;

    @OneToMany(cascade = CascadeType.ALL,mappedBy="stock")
    private List<StockTransactions> stockTransaction;

    @OneToMany(cascade=CascadeType.ALL, mappedBy="stock")
    private List<StockPriceHistories> stockPriceHistories;

    @OneToMany(cascade=CascadeType.ALL, mappedBy="stock")
    private List<OwnedStocks> ownedStocks;

    @OneToOne(cascade=CascadeType.ALL, mappedBy="stock")
    private OrderBooks orderBook;
}
