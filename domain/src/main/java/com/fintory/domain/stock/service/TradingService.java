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

}
