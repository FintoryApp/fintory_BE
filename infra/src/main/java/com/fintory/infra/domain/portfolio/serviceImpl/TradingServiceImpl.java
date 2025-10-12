package com.fintory.infra.domain.portfolio.serviceImpl;


import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.account.model.Account;
import com.fintory.domain.account.model.DepositTransaction;
import com.fintory.domain.account.model.DepositTransactionType;
import com.fintory.domain.child.model.Child;
import com.fintory.domain.portfolio.dto.TradeCalculation;
import com.fintory.domain.portfolio.dto.TradeRequest;
import com.fintory.domain.portfolio.model.*;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.portfolio.service.ExchangeRateService;
import com.fintory.domain.portfolio.service.TradingService;
import com.fintory.infra.domain.account.repository.AccountRepository;
import com.fintory.infra.domain.account.repository.DepositTransactionRepository;
import com.fintory.infra.domain.child.repository.ChildRepository;
import com.fintory.infra.domain.portfolio.repository.OwnedStockRepository;
import com.fintory.infra.domain.portfolio.repository.StockTransactionRepository;
import com.fintory.infra.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradingServiceImpl implements TradingService {

    private final ExchangeRateService exchangeRateService;
    private final ChildRepository childRepository;
    private final AccountRepository accountRepository;
    private final StockRepository stockRepository;
    private final OwnedStockRepository ownedStockRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final DepositTransactionRepository depositTransactionRepository;


    @Override
    @Transactional
    public void trade(TradeRequest tradeRequest, String email){
        Child child = childRepository.findByEmail(email).orElseThrow(()-> new DomainException(DomainErrorCode.USER_NOT_FOUND));
        Account account = accountRepository.findByChildId(child.getId()).orElseThrow(()-> new DomainException(DomainErrorCode.ACCOUNT_NOT_FOUND));
        Stock stock = stockRepository.findByCode(tradeRequest.stockCode()).orElseThrow(()-> new DomainException(DomainErrorCode.STOCK_NOT_FOUND));

        BigDecimal exchangeRate = exchangeRateService.getExchangeRate();

        if (tradeRequest.transactionType() == TransactionType.BUY) {
            processBuyTrade(tradeRequest, account, stock, exchangeRate);
        } else {
            processSellTrade(tradeRequest, account, stock, exchangeRate);
        }
    }

    //주식 구매 기능
    private void processBuyTrade(TradeRequest tradeRequest, Account account, Stock stock, BigDecimal exchangeRate){


        TradeCalculation tradeCalculation = calculateTradeAmount(tradeRequest, stock, exchangeRate);
        BigDecimal totalTradeAmount = tradeCalculation.amount();
        MarketType marketType =  tradeCalculation.marketType();

        //현재 account 금액을 넘지 않는지 확인
        if(!isAvailablePurchase(account, totalTradeAmount)){
            throw new DomainException(DomainErrorCode.INSUFFICIENT_BALANCE);
        }

        //ownedStock 업데이트/생성 -> setter/빌더, stockTransaction 생성 -> 빌더
        updateStockAndTransactionForPurchase(tradeRequest, account, stock, totalTradeAmount, exchangeRate, marketType);

        // 현금거래 내역 생성 -> 정적 팩토리 메소드
        DepositTransaction depositTransaction = DepositTransaction.create(totalTradeAmount.negate(), stock.getName() + "매수", DepositTransactionType.WITHDRAW, account);
        depositTransactionRepository.save(depositTransaction);
        //account 업데이트 -> setter
        updateAccountForPurchase(account, totalTradeAmount);
    }


    //주식 판매 기능
    private void processSellTrade(TradeRequest tradeRequest, Account account, Stock stock, BigDecimal exchangeRate){

        TradeCalculation tradeCalculation = calculateTradeAmount(tradeRequest, stock, exchangeRate);
        BigDecimal totalTradeAmount = tradeCalculation.amount(); // 환율 적용
        MarketType marketType =  tradeCalculation.marketType();

        OwnedStock ownedStock = ownedStockRepository.findByAccountAndStock(account, stock).orElseThrow(()-> new DomainException(DomainErrorCode.OWNED_STOCK_NOT_FOUND));


        //현재 주식 판매가 가능한지
        if(!isAvailableSell(tradeRequest,ownedStock)){
            throw new DomainException(DomainErrorCode.INSUFFICIENT_BALANCE);
        }

        // 매도신청할 수량 X 매수평균가
        BigDecimal soldPurchaseAmount = ownedStock.getAveragePurchasePrice()
                .multiply(tradeRequest.quantity());

        //ownedStock, stockTransaction 업데이트
        updateStockAndTransactionForSell(ownedStock, tradeRequest, account, stock, totalTradeAmount, exchangeRate, marketType, soldPurchaseAmount);

        // 현금 내역 생성 -> 정적 팩토리 메소드
        DepositTransaction depositTransaction = DepositTransaction.create(totalTradeAmount, stock.getName() + "매도", DepositTransactionType.DEPOSIT, account);
        depositTransactionRepository.save(depositTransaction);

        //account 업데이트 -> setter
        updateAccountForSell(account, totalTradeAmount, soldPurchaseAmount);


    }

    private boolean isAvailablePurchase(Account account, BigDecimal purchasePrice){
        //구매할 수 없다면
        return account.getAvailableCash().compareTo(purchasePrice) >= 0;
    }

    private boolean isAvailableSell(TradeRequest tradeRequest, OwnedStock ownedStock){
        return ownedStock.getQuantity().compareTo(tradeRequest.quantity()) >= 0;
    }

    private void updateAccountForPurchase(Account account, BigDecimal purchasePrice){
        account.updatePurchaseStock(purchasePrice);
    }

    private void updateAccountForSell(Account account, BigDecimal purchasePrice, BigDecimal soldPurchaseAmount){
        // 계좌 업데이트
        account.updateSellStock(purchasePrice, soldPurchaseAmount);

    }


    private void updateStockAndTransactionForPurchase(TradeRequest tradeRequest, Account account, Stock stock, BigDecimal totalTradeAmount, BigDecimal exchangeRate, MarketType marketType){
        OwnedStock ownedStock = ownedStockRepository.findByAccountAndStock(account, stock)
                .orElse(null);

        //해외 주식이면 환율 적용
        BigDecimal averagePurchasePrice = calculatePriceWithExchange(tradeRequest.price(), marketType,exchangeRate);

        // 새로 구매한 주식일 경우
        if(ownedStock == null){
            ownedStock = OwnedStock.builder()
                    .account(account)
                    .stock(stock)
                    .purchaseAmount(totalTradeAmount)
                    .quantity(tradeRequest.quantity())
                    .averagePurchasePrice(averagePurchasePrice)
                    .build();

            ownedStockRepository.save(ownedStock);
        }else{
            // 기존에 구매한 주식이 있을 경우
            ownedStock.updateOwnedStockPurchase(tradeRequest.quantity(), totalTradeAmount);
        }


        // 주식 거래 내역 업데이트
        StockTransaction stockTransaction = StockTransaction.builder()
                .pricePerShare(tradeRequest.price())
                .amount(totalTradeAmount)
                .quantity(tradeRequest.quantity())
                .executedAt(LocalDateTime.now())
                .exchangeRate(exchangeRate)
                .transactionType(TransactionType.BUY)
                .marketType(marketType)
                .stock(stock)
                .account(account)
                .build();

        stockTransactionRepository.save(stockTransaction);

    }

    private void updateStockAndTransactionForSell(OwnedStock ownedStock, TradeRequest tradeRequest, Account account, Stock stock, BigDecimal totalTradeAmount,
                                                 BigDecimal exchangeRate, MarketType marketType, BigDecimal soldPurchaseAmount){

        ownedStock.updateOwnedStockSell(tradeRequest.quantity(), soldPurchaseAmount);

        if (ownedStock.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
            ownedStockRepository.delete(ownedStock);
        } else {
            ownedStockRepository.save(ownedStock);
        }

        // 주식 거래 내역 업데이트
        StockTransaction stockTransaction = StockTransaction.builder()
                .pricePerShare(tradeRequest.price())
                .amount(totalTradeAmount)
                .quantity(tradeRequest.quantity())
                .executedAt(LocalDateTime.now())
                .exchangeRate(exchangeRate)
                .transactionType(TransactionType.SELL)
                .marketType(marketType)
                .stock(stock)
                .account(account)
                .build();

        stockTransactionRepository.save(stockTransaction);

    }

    /* 헬퍼 메서드 */
    private TradeCalculation calculateTradeAmount(TradeRequest tradeRequest, Stock stock, BigDecimal exchangeRate){
        BigDecimal amount;
        MarketType marketType;
        if(stock.getCurrencyName().equals("USD")) {
            amount = tradeRequest.price().multiply(tradeRequest.quantity()).multiply(exchangeRate);
            marketType= MarketType.OVERSEAS;
        } else {
            amount = tradeRequest.price().multiply(tradeRequest.quantity());
            marketType = MarketType.DOMESTIC;
        }
        return new TradeCalculation(amount,marketType);
    }

    private BigDecimal calculatePriceWithExchange(BigDecimal price, MarketType marketType, BigDecimal exchangeRate) {
        return marketType.equals(MarketType.OVERSEAS) ? price.multiply(exchangeRate) : price;
    }
}
