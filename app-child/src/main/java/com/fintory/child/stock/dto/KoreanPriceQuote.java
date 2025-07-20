package com.fintory.child.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintory.domain.stock.model.OrderBook;
import com.fintory.domain.stock.model.Stock;
import lombok.*;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KoreanPriceQuote {

    private String code;

    private BigDecimal ASKP1;

    private BigDecimal ASKP2;

    private BigDecimal ASKP3;

    private BigDecimal ASKP4;

    private BigDecimal ASKP5;

    private BigDecimal ASKP6;

    private BigDecimal ASKP7;

    private BigDecimal ASKP8;

    private BigDecimal ASKP9;

    private BigDecimal ASKP10;


    private BigDecimal BIDP1;

    private BigDecimal BIDP2;

    private BigDecimal BIDP3;

    private BigDecimal BIDP4;

    private BigDecimal BIDP5;

    private BigDecimal BIDP6;

    private BigDecimal BIDP7;

    private BigDecimal BIDP8;

    private BigDecimal BIDP9;

    private BigDecimal BIDP10;


    @JsonProperty("ASKP_RSQN1")
    private Long ASKPRSQN1;

    @JsonProperty("ASKP_RSQN2")
    private Long ASKPRSQN2;

    @JsonProperty("ASKP_RSQN3")
    private Long ASKPRSQN3;

    @JsonProperty("ASKP_RSQN4")
    private Long ASKPRSQN4;

    @JsonProperty("ASKP_RSQN5")
    private Long ASKPRSQN5;

    @JsonProperty("ASKP_RSQN6")
    private Long ASKPRSQN6;

    @JsonProperty("ASKP_RSQN7")
    private Long ASKPRSQN7;

    @JsonProperty("ASKP_RSQN8")
    private Long ASKPRSQN8;

    @JsonProperty("ASKP_RSQN9")
    private Long ASKPRSQN9;

    @JsonProperty("ASKP_RSQN10")
    private Long ASKPRSQN10;

    @JsonProperty("BIDP_RSQN1")
    private Long BIDPRSQN1;

    @JsonProperty("BIDP_RSQN2")
    private Long BIDPRSQN2;

    @JsonProperty("BIDP_RSQN3")
    private Long BIDPRSQN3;

    @JsonProperty("BIDP_RSQN4")
    private Long BIDPRSQN4;

    @JsonProperty("BIDP_RSQN5")
    private Long BIDPRSQN5;

    @JsonProperty("BIDP_RSQN6")
    private Long BIDPRSQN6;

    @JsonProperty("BIDP_RSQN7")
    private Long BIDPRSQN7;

    @JsonProperty("BIDP_RSQN8")
    private Long BIDPRSQN8;

    @JsonProperty("BIDP_RSQN9")
    private Long BIDPRSQN9;

    @JsonProperty("BIDP_RSQN10")
    private Long BIDPRSQN10;

    public static OrderBook toOrderBook(KoreanPriceQuote koreanPriceQuote, Stock stock){
        return OrderBook.builder()
                .sellPrice1(koreanPriceQuote.getASKP1())
                .sellPrice2(koreanPriceQuote.getASKP2())
                .sellPrice3(koreanPriceQuote.getASKP3())
                .sellPrice4(koreanPriceQuote.getASKP4())
                .sellPrice5(koreanPriceQuote.getASKP5())
                .sellPrice6(koreanPriceQuote.getASKP6())
                .sellPrice7(koreanPriceQuote.getASKP7())
                .sellPrice8(koreanPriceQuote.getASKP8())
                .sellPrice9(koreanPriceQuote.getASKP9())
                .sellPrice10(koreanPriceQuote.getASKP10())
                .buyPrice1(koreanPriceQuote.getBIDP1())
                .buyPrice2(koreanPriceQuote.getBIDP2())
                .buyPrice3(koreanPriceQuote.getBIDP3())
                .buyPrice4(koreanPriceQuote.getBIDP4())
                .buyPrice5(koreanPriceQuote.getBIDP5())
                .buyPrice6(koreanPriceQuote.getBIDP6())
                .buyPrice7(koreanPriceQuote.getBIDP7())
                .buyPrice8(koreanPriceQuote.getBIDP8())
                .buyPrice9(koreanPriceQuote.getBIDP9())
                .buyPrice10(koreanPriceQuote.getBIDP10())
                .sellQuantity1(koreanPriceQuote.getASKPRSQN1())
                .sellQuantity2(koreanPriceQuote.getASKPRSQN2())
                .sellQuantity3(koreanPriceQuote.getASKPRSQN3())
                .sellQuantity4(koreanPriceQuote.getASKPRSQN4())
                .sellQuantity5(koreanPriceQuote.getASKPRSQN5())
                .sellQuantity6(koreanPriceQuote.getASKPRSQN6())
                .sellQuantity7(koreanPriceQuote.getASKPRSQN7())
                .sellQuantity8(koreanPriceQuote.getASKPRSQN8())
                .sellQuantity9(koreanPriceQuote.getASKPRSQN9())
                .sellQuantity10(koreanPriceQuote.getASKPRSQN10())
                .buyQuantity1(koreanPriceQuote.getBIDPRSQN1())
                .buyQuantity2(koreanPriceQuote.getBIDPRSQN2())
                .buyQuantity3(koreanPriceQuote.getBIDPRSQN3())
                .buyQuantity4(koreanPriceQuote.getBIDPRSQN4())
                .buyQuantity5(koreanPriceQuote.getBIDPRSQN5())
                .buyQuantity6(koreanPriceQuote.getBIDPRSQN6())
                .buyQuantity7(koreanPriceQuote.getBIDPRSQN7())
                .buyQuantity8(koreanPriceQuote.getBIDPRSQN8())
                .buyQuantity9(koreanPriceQuote.getBIDPRSQN9())
                .buyQuantity10(koreanPriceQuote.getBIDPRSQN10())
                .stock(stock)
                .build();

    }

}
