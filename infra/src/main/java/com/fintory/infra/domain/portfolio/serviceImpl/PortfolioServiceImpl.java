package com.fintory.infra.domain.portfolio.serviceImpl;

import com.fintory.domain.account.model.Account;
import com.fintory.domain.portfolio.dto.OwnedStockList;
import com.fintory.domain.portfolio.dto.PortfolioSummary;
import com.fintory.domain.portfolio.model.OwnedStock;
import com.fintory.domain.portfolio.model.StockTransaction;
import com.fintory.domain.portfolio.model.TransactionType;
import com.fintory.domain.portfolio.service.PortfolioService;
import com.fintory.domain.stock.model.Stock;
import com.fintory.infra.domain.account.repository.AccountRepository;
import com.fintory.infra.domain.portfolio.repository.OwnedStockRepository;
import com.fintory.infra.domain.portfolio.repository.StockTransactionRepository;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final StockTransactionRepository stockTransactionRepository;
    private final OwnedStockRepository ownedStockRepository;
    private final AccountRepository accountRepository;

    public List<OwnedStockList>  getOwendStockList() {
        List<OwnedStockList> list = new ArrayList<>();
        List<OwnedStock> ownedStocks = ownedStockRepository.findAll();
        for (OwnedStock own : ownedStocks) {
            Map<String, BigDecimal> ownedMap =getTransaction(own.getStock());
            list.add(OwnedStockList.builder().stockCode(own.getStock().getCode()).stockName(own.getStock().getName()).evaluationAmount(ownedMap.get("evaluationAmount")).profit(ownedMap.get("profit")).returnRate(ownedMap.get("returnRate")).build());
        }
        return list;
    }

    public PortfolioSummary getPortfolioSummary(){
        Account account = accountRepository.findByChildId(1L);
        List<OwnedStock> ownedStocks = ownedStockRepository.findAll();
        Map<String, BigDecimal> ownedMap;

        BigDecimal totalEvaluationAmount = BigDecimal.ZERO;
        BigDecimal totalPurchasePrice = BigDecimal.ZERO;
        BigDecimal totalReturnRate = BigDecimal.ZERO;

        for (OwnedStock own : ownedStocks) {
           ownedMap =getTransaction(own.getStock());
            totalEvaluationAmount= totalEvaluationAmount.add(ownedMap.get("evaluationAmount"));
            totalPurchasePrice=totalPurchasePrice.add(ownedMap.get("totalPurchasePrice"));
        }

        BigDecimal totalProfit = totalEvaluationAmount.subtract(totalPurchasePrice);

        if(totalPurchasePrice.compareTo(BigDecimal.ZERO) > 0) {
            totalReturnRate = totalProfit.divide(totalPurchasePrice, 2, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
        }

        return PortfolioSummary.builder()
                .totalEvaluationAmount(totalEvaluationAmount)
                .totalReturnRate(totalReturnRate)
                .totalPurchasePrice(totalPurchasePrice)
                .totalMoney(BigDecimal.valueOf(account.getTotalAssets()))
                .build();
    }

    private Map<String, BigDecimal> getTransaction(Stock stock){
        Map<String, BigDecimal> map = new HashMap<>();
        List<StockTransaction> transactionList = stockTransactionRepository.findByStock(stock);

        BigDecimal totalPurchasePrice = BigDecimal.ZERO;
        int totalQuantity = 0;

        for(StockTransaction transaction : transactionList){
            if(transaction.getTransactionType().equals(TransactionType.BUY)) { //BUY
                totalPurchasePrice = totalPurchasePrice.add(
                        transaction.getPricePerShare().multiply(new BigDecimal(transaction.getQuantity()))
                );
                totalQuantity += transaction.getQuantity();
            } else { // SELL
                totalPurchasePrice = totalPurchasePrice.subtract(
                        transaction.getPricePerShare().multiply(new BigDecimal(transaction.getQuantity()))
                ).setScale(0, RoundingMode.HALF_UP);
                totalQuantity -= transaction.getQuantity();

                // 0주가 되면 초기화
                if(totalQuantity == 0) {
                    totalPurchasePrice = BigDecimal.ZERO;
                }
            }
        }

        // 현재 평가금액과 수익 계산
        if(totalQuantity > 0) {
            BigDecimal currentPrice = BigDecimal.valueOf(stock.getLiveStockPrice().getCurrentPrice());

            // USD면 환율 적용
            if(stock.getCurrencyName().equals("USD")) {
                BigDecimal exchangeRate =  BigDecimal.valueOf(1300); //환율 테이블 찾기
                totalPurchasePrice = totalPurchasePrice.multiply(exchangeRate);
                currentPrice = currentPrice.multiply(exchangeRate);
            }

            BigDecimal currentValue = currentPrice.multiply(new BigDecimal(totalQuantity));
            BigDecimal profit = currentValue.subtract(totalPurchasePrice).setScale(0,RoundingMode.HALF_UP);

            map.put("evaluationAmount", currentValue);
            map.put("profit", profit);
            map.put("totalPurchasePrice", totalPurchasePrice);

            if(totalPurchasePrice.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal returnRate = profit.divide(totalPurchasePrice, 2, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
                map.put("returnRate", returnRate);
            } else {
                map.put("returnRate", BigDecimal.ZERO);
            }
        } else {
            // 보유 주식이 없는 경우
            map.put("evaluationAmount", BigDecimal.ZERO);
            map.put("profit", BigDecimal.ZERO);
            map.put("totalPurchasePrice", BigDecimal.ZERO);
            map.put("returnRate", BigDecimal.ZERO);
        }

        return map;
    }
}
