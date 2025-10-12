package com.fintory.infra.domain.portfolio.serviceImpl;

import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.account.model.Account;
import com.fintory.domain.child.model.Child;
import com.fintory.domain.portfolio.dto.*;
import com.fintory.domain.portfolio.dto.summary.PortfolioSummaryResponse;
import com.fintory.domain.portfolio.model.OwnedStock;
import com.fintory.domain.portfolio.model.StockTransaction;
import com.fintory.domain.portfolio.model.TransactionType;
import com.fintory.domain.portfolio.service.PortfolioService;
import com.fintory.domain.stock.dto.korean.response.KoreanLiveStockPriceResponse;
import com.fintory.domain.stock.dto.overseas.response.OverseasLiveStockPriceResponse;
import com.fintory.domain.stock.model.Stock;
import com.fintory.domain.stock.service.korean.KoreanLiveStockPriceService;
import com.fintory.domain.stock.service.overseas.OverseasLiveStockPriceService;
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
    private final KoreanLiveStockPriceService koreanLiveStockPriceService;
    private final OverseasLiveStockPriceService overseasLiveStockPriceService;

    @Transactional(readOnly = true)
    public List<OwnedStockMetrics>  getOwnedStockMetrics() {
        Account account = accountRepository.findByChildId(1L).orElseThrow(()-> new DomainException(DomainErrorCode.ACCOUNT_NOT_FOUND));
        try {
            List<OwnedStock> ownedStocks = ownedStockRepository.findByAccount(account); //로그인 기능 이후 리팩토링 예정
            return ownedStocks.stream().map(ownedStock->{
                //사용자의 거래 내역 db로부터 조회
                List<StockTransaction> transactionList = stockTransactionRepository.findByStockOrderByExecutedAt(ownedStock.getStock());

                // 필요한 필드로만 구성해서 dto 생성
                List<StockTransactionInfo> stockTransactionInfos = transactionList.stream().map(transaction->
                     new StockTransactionInfo(
                            transaction.getPricePerShare(),
                            transaction.getQuantity(),
                            transaction.getExchangeRate(),
                            transaction.getTransactionType(),
                            transaction.getExecutedAt()
                     )).collect(Collectors.toList());

                StockMetricsResult result = calculateCurrentMetrics(transactionList);

                BigDecimal currentPrice = getCurrentPrice(ownedStock.getStock());

                // 주식별 거래내역이 포함된 OwnedStockMetrics 생성
                return new OwnedStockMetrics(
                        ownedStock.getStock().getCode(),
                        ownedStock.getStock().getName(),
                        result.avgPurchasePrice(),
                        result.currentQuantity(),
                        currentPrice,
                        stockTransactionInfos
                );
            }).collect(Collectors.toList());

        }catch(Exception e){
            log.info("소유 종목 리스트 조회 시 에러 발생:{}",e.getMessage());
            throw new DomainException(DomainErrorCode.OWNED_STOCK_LIST_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public PortfolioSummary getPortfolioSummary(){
        Account account = accountRepository.findByChildId(1L).orElseThrow(()-> new DomainException(DomainErrorCode.ACCOUNT_NOT_FOUND)); // 로그인 기능 완성되면 @AuthencationPrincipal로 주입받을 예정

        try {
            List<OwnedStock> ownedStocks = ownedStockRepository.findByAccount(account);

            // 총 매수 금액
            BigDecimal totalPurchasePrice = ownedStocks.stream()
                    .map(ownedStock->{
                        List<StockTransaction> transactionList = stockTransactionRepository.findByStockOrderByExecutedAt(ownedStock.getStock());
                        return calculateCurrentMetrics(transactionList).totalInvestment();
                    }).reduce(BigDecimal.ZERO,BigDecimal::add);

            return new PortfolioSummary(
                    totalPurchasePrice
                    );
        }catch (Exception e){
            log.error("포트폴리오 요약 조회 시 에러 발생: {}", e.getMessage());
            throw new DomainException(DomainErrorCode.PORTFOLIO_CALCULATION_ERROR);
        }
    }

    public PortfolioSummaryResponse getPortfolioSummaryResponseByChild(Child child) {
        Account account = accountRepository.findByChild(child)
                .orElseThrow(() -> new DomainException(DomainErrorCode.ACCOUNT_NOT_FOUND));

        List<OwnedStock> ownedStocks = ownedStockRepository.findAllWithStockByAccount(account);

        BigDecimal currentPrice = getCurrentPrice(ownedStocks.get(0).getStock());
        return PortfolioSummaryResponse.from(account, ownedStocks,currentPrice);
    }

    //현재가 조회 메소드
    private BigDecimal getCurrentPrice(Stock stock) {
        if ("KRW".equals(stock.getCurrencyName())) {
            KoreanLiveStockPriceResponse response = koreanLiveStockPriceService.getLiveStockPriceViaQuery(stock);
            return response.currentPrice();
        } else if ("USD".equals(stock.getCurrencyName())) {
            OverseasLiveStockPriceResponse response = overseasLiveStockPriceService.getLiveStockPriceViaQuery(stock);
            return response.currentPrice();
        }
        throw new IllegalStateException("지원하지 않는 통화: " + stock.getCurrencyName());
    }

    //주식별 평균 매수가, 총 매수 수량, 총 매수가 계산
    private StockMetricsResult calculateCurrentMetrics(List<StockTransaction> stockTransactionList){
        BigDecimal totalInvestment = BigDecimal.ZERO;
        BigDecimal totalQuantity = BigDecimal.ZERO;

        for(StockTransaction transaction:stockTransactionList){
            //거래 금액 = 주당가격 * 보유수량 *환율 반영
            BigDecimal amount = transaction.getPricePerShare()
                    .multiply(transaction.getQuantity())
                    .multiply(transaction.getExchangeRate()); //USD=1300, KRW는 1.0이라는 가정

            if(transaction.getTransactionType().equals(TransactionType.BUY)){
                //매수 시 투자금액과 보유수량 증가
                totalInvestment = totalInvestment.add(amount);
                totalQuantity =  totalQuantity.add(transaction.getQuantity());
            }else{
                if(totalQuantity.compareTo(BigDecimal.ZERO) > 0){
                    //매도 시 평균 매수가 기준으로 처리
                    BigDecimal avgPrice =totalInvestment.divide(totalQuantity,0, RoundingMode.HALF_UP);
                    BigDecimal sellAmount = avgPrice.multiply(transaction.getQuantity());

                    totalInvestment = totalInvestment.subtract(sellAmount);
                    totalQuantity = totalQuantity.subtract(transaction.getQuantity());
                }
            }
        }
        //0으로 나누기 방지
        BigDecimal avgPurchasePrice = totalQuantity.compareTo(BigDecimal.ZERO) > 0
                ? totalInvestment.divide(totalQuantity,0, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;


        return new StockMetricsResult(
                avgPurchasePrice,
                totalQuantity.intValue(),
                totalInvestment.setScale(0, RoundingMode.HALF_UP)
        );
    }


}
