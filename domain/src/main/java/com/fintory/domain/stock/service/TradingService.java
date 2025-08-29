package com.fintory.domain.stock.service;

import com.fintory.domain.account.model.Account;
import com.fintory.domain.portfolio.dto.TradeRequest;
import com.fintory.domain.portfolio.model.TransactionType;
import com.fintory.domain.stock.model.Stock;

import java.math.BigDecimal;

public interface TradingService {

    /**
     *  주식 거래 템플릿 메서드
     * @param tradeRequest 거래하고자 하는 주식 종목 코드, 수량, 현재가
     * @param email 로그인한 사용자의 이메일
     *
     */
    void trade(TradeRequest tradeRequest, String email);


    /**
     *  주식 매수 처리 메서드
     * @param tradeRequest 거래하고자 하는 주식 종목 코드, 수량, 현재가
     * @param account 현재 로그인한 사용자의 계좌
     * @param stock 거래하고자 하는 주식 종목
     * @param exchangeRate 환율
     */
    void processBuyTrade(TradeRequest tradeRequest, Account account, Stock stock, BigDecimal exchangeRate);


    /**
     *  주식 매도 처리 메서드
     * @param tradeRequest 거래하고자 하는 주식 종목 코드, 수량, 현재가
     * @param account 현재 로그인한 사용자의 계좌
     * @param stock 거래하고자 하는 주식 종목
     * @param exchangeRate 환율
     */
    void processSellTrade(TradeRequest tradeRequest,Account account, Stock stock, BigDecimal exchangeRate);
}
