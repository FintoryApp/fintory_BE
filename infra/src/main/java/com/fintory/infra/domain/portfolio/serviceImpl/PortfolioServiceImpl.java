package com.fintory.infra.domain.portfolio.serviceImpl;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final StockTransactionRepository stockTransactionRepository;
    private final OwnedStockRepository ownedStockRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public List<OwnedStockList>  getOwnedStockList() {
        try {
            Account account = accountRepository.findByChildId(1L);
            List<OwnedStock> ownedStocks = ownedStockRepository.findByAccount(account); //로그인 기능 이후 리팩토링 예정

            return ownedStocks.stream()
                    .map(stock -> {
                        Map<String, BigDecimal> ownedMap = getByTransaction(stock.getStock());
                        return new OwnedStockList(stock.getStock().getCode(),
                                        stock.getStock().getName(),
                                ownedMap.get("evaluationAmount"),
                                ownedMap.get("profit"),
                                ownedMap.get("returnRate"));
                    }).collect(Collectors.toList());
        }catch(Exception e){
            log.info("소유 종목 리스트 조회 시 에러 발생:{}",e.getMessage());
            throw new DomainException(DomainErrorCode.OWNED_STOCK_LIST_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public PortfolioSummary getPortfolioSummary(){
        try {
            Account account = accountRepository.findByChildId(1L); // 로그인 기능 완성되면 @AuthencationPrincipal로 주입받을 예정
            List<OwnedStock> ownedStocks = ownedStockRepository.findByAccount(account);
            Map<String, BigDecimal> ownedMap;

            BigDecimal totalEvaluationAmount = BigDecimal.ZERO; //총 평가 금액
            BigDecimal totalPurchasePrice = BigDecimal.ZERO; // 총 매수 금액
            BigDecimal totalReturnRate = BigDecimal.ZERO; //수익률

            // 사용자가 소유한 주식 대상
            for (OwnedStock own : ownedStocks) {
                ownedMap = getByTransaction(own.getStock());
                totalEvaluationAmount = totalEvaluationAmount.add(ownedMap.get("evaluationAmount"));
                totalPurchasePrice = totalPurchasePrice.add(ownedMap.get("totalPurchasePrice"));
            }

            BigDecimal totalProfit = totalEvaluationAmount.subtract(totalPurchasePrice); //총 손실 금액

            if (totalPurchasePrice.compareTo(BigDecimal.ZERO) > 0) {
                totalReturnRate = totalProfit.divide(totalPurchasePrice, 2, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
            }
            return new PortfolioSummary(
                    totalEvaluationAmount,
                    totalReturnRate,
                    totalPurchasePrice.setScale(0, RoundingMode.HALF_UP),
                    BigDecimal.valueOf(account.getTotalAssets()));
        }catch (Exception e){
            log.error("포트폴리오 요약 조회 시 에러 발생: {}", e.getMessage());
            throw new DomainException(DomainErrorCode.PORTFOLIO_CALCULATION_ERROR);
        }
    }

    // 거래 내역으로부터 데이터 조회
    private Map<String, BigDecimal> getByTransaction(Stock stock){
        List<StockTransaction> transactionList = stockTransactionRepository.findByStock(stock);

        // 매수/매도 거래로부터 특정 주식에 해당하는 현재 보유 매수금액과 보유 수량 계산
        Map<String, BigDecimal> map = calculateCurrentHolding
                (transactionList);

        // 보유 주식이 없는 경우
        if(map.get("totalQuantity").compareTo(BigDecimal.ZERO) <= 0){
            return createEmptyResult(new HashMap<>());
        }

        map =  calculateStockMetrics(stock,map);
        return map;
    }

    // 매수/매도 거래로부터 특정 주식에 해당하는 현재 보유 매수금액과 보유 수량 계산
    private Map<String, BigDecimal> calculateCurrentHolding (List<StockTransaction> transactionList){
        BigDecimal totalPurchasePrice = BigDecimal.ZERO; //현재 보유 주식의 총 매수 금액
        BigDecimal totalQuantity = BigDecimal.ZERO; //현재 보유 수량

        // 매수/매도 계산
        for(StockTransaction transaction : transactionList){
            if(TransactionType.BUY.equals(transaction.getTransactionType())) { //BUY
                totalPurchasePrice = totalPurchasePrice.add(
                        transaction.getPricePerShare().multiply(BigDecimal.valueOf(transaction.getQuantity()))
                );
                totalQuantity = totalQuantity.add(BigDecimal.valueOf(transaction.getQuantity())); // 재할당!
            } else { // SELL
                totalPurchasePrice = totalPurchasePrice.subtract(
                        transaction.getPricePerShare().multiply(BigDecimal.valueOf(transaction.getQuantity()))
                ).setScale(0, RoundingMode.HALF_UP);
                totalQuantity = totalQuantity.subtract(BigDecimal.valueOf(transaction.getQuantity())); // 재할당!

                // 0주가 되면 초기화
                if(totalQuantity.compareTo(BigDecimal.ZERO) == 0) {
                    totalPurchasePrice = BigDecimal.ZERO;
                }
            }
        }

        Map<String, BigDecimal> result = new HashMap<>();
        result.put("totalPurchasePrice", totalPurchasePrice);
        result.put("totalQuantity", totalQuantity);
        return result;
    }

    //현재 평가 금액 및 수익/손실 계산
    private Map<String,BigDecimal> calculateStockMetrics(Stock stock, Map<String,BigDecimal> map){

        BigDecimal totalPurchasePrice = map.get("totalPurchasePrice");
        BigDecimal totalQuantity = map.get("totalQuantity");

        // 현재 평가금액과 수익 계산
        BigDecimal currentPrice = BigDecimal.valueOf(stock.getLiveStockPrice().getCurrentPrice());

        // USD면 환율 적용
        if("USD".equals(stock.getCurrencyName())) {
            BigDecimal exchangeRate = BigDecimal.valueOf(1300); //환율 테이블 찾기
            totalPurchasePrice = totalPurchasePrice.multiply(exchangeRate);
            currentPrice = currentPrice.multiply(exchangeRate);
        }

        BigDecimal currentValue = currentPrice.multiply(totalQuantity);
        BigDecimal profit = currentValue.subtract(totalPurchasePrice).setScale(0, RoundingMode.HALF_UP);

        map.put("evaluationAmount", currentValue);
        map.put("profit", profit);
        map.put("totalPurchasePrice", totalPurchasePrice); // 환율 적용된 값으로 업데이트
        map.put("returnRate",calculateReturnRate(profit,totalPurchasePrice));
        return map;
    }

    // 수익률 계산
    private BigDecimal calculateReturnRate(BigDecimal profit, BigDecimal totalPurchasePrice){
        if(totalPurchasePrice.compareTo(BigDecimal.ZERO) > 0) {
            return profit.divide(totalPurchasePrice, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }


    //보유 주식이 없는 경우
    private Map<String,BigDecimal> createEmptyResult(Map<String,BigDecimal> map){
        map.put("evaluationAmount", BigDecimal.ZERO);
        map.put("profit", BigDecimal.ZERO);
        map.put("totalPurchasePrice", BigDecimal.ZERO);
        map.put("returnRate", BigDecimal.ZERO);
        return map;
    }

}
