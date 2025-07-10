package com.fintory.child.consulting.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintory.child.consulting.dto.*;
import com.fintory.child.portfolio.service.StockTransactionService;
import com.fintory.common.exception.BaseException;
import com.fintory.common.exception.ErrorCode;

import com.fintory.domain.consulting.model.Report;
import com.fintory.domain.portfolio.model.OwnedStock;
import com.fintory.domain.portfolio.model.StockTransaction;
import com.fintory.domain.portfolio.model.TransactionType;
import com.fintory.domain.portfolio.repository.OwnedStockRepository;
import com.fintory.domain.consulting.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;


import static com.fintory.child.consulting.dto.ReportDetail.*;

@Slf4j
@Service
public class ConsultingService {

    private static final String TEMP_CHILD_ID = "TEST";
    private static final String TEMP_CHILD_NAME="TEST";

    private final ChatClient chatClient;
    private final StockTransactionService stockTransactionService;
    private final ReportRepository reportRepository;
    private final OwnedStockRepository ownedStockRepository;
//
//    @Value("${spring.ai.openai.api-key}")
//    private String apiKey;

    private static final String API_BASE_URL = "https://api.openai.com/";
    private static final String API_KEY ="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";
    public ConsultingService(

            StockTransactionService stockTransactionService,
            ReportRepository reportRepository,
            OwnedStockRepository ownedStockRepository
    ) {

       RestClient.Builder restClientBuilder = RestClient.builder()
               .defaultHeader("Authorization","Bearer "+API_KEY)
               .defaultHeader("Accept-Encoding","identity");

       WebClient.Builder webClientBuilder = WebClient.builder()
               .defaultHeader("Authorization","Bearer "+API_KEY)
               .defaultHeader("Accept-Encoding","identity");


       OpenAiApi openAiApi = new OpenAiApi(API_BASE_URL,API_KEY,restClientBuilder,webClientBuilder);

        OpenAiChatModel model = new OpenAiChatModel(openAiApi);

       this.chatClient = ChatClient.builder(model).build();
        this.stockTransactionService = stockTransactionService;
        this.reportRepository = reportRepository;
        this.ownedStockRepository = ownedStockRepository;

    }


    public AiResponse getConsulting(List<StockTransaction> stockTransactions) {
        try {
            //거래 내역 문자열로 변환
            String transactionData = formatTransactionsData(stockTransactions);

            log.info("Consulting transaction data: {}", transactionData);

            String response = chatClient.prompt()
                    .system("""
        당신은 주식 트레이딩 전문가입니다.\s
        거래 내역을 분석하여 투자 성향과 조언을 제공하는 역할입니다.
        반드시 정확한 JSON 형식으로만 응답해주세요.
       \s""")
                    .user("""
        다음 거래 내역을 분석해주세요:
        %s
        
        위 거래 내역을 바탕으로 다음을 분석하여 JSON 형식으로 응답해주세요:
        1. 투자 성향 (공격형, 중립형, 안정형 중 하나)
        2. 향후 전략 추천 메시지
        
        응답 형식:
        {
            "investmentStyle": "공격형",
            "advice": "향후 전략 추천 메시지"
        }
        """.formatted(transactionData))
                    .call()
                    .content();

            log.info("AI 원본 응답: {}", response);

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(response, AiResponse.class);
        }catch (JsonProcessingException e){
            e.printStackTrace();
            throw new BaseException(ErrorCode.JSON_PROCESSING_FAILED);
        }
    }

    public ReportDetail getDetailConsulting(Long reportId) {
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new IllegalArgumentException("발견할 수 없는 리포트입니다."));
        return fromReport(report);

    }

    public ReportDetail getConsultingByDate(String date)  {
            Report report = reportRepository.findByReportMonth(date).orElseThrow(()-> new BaseException(ErrorCode.REPORT_NOT_FOUND));
            return fromReport(report);
    }


    //저장하는 로직
    public void saveReportDetail(List<StockTransaction> stockTransactions){
        try {
            YearMonth reportMonth = YearMonth.now();
            List<OwnedStock> ownedStocks = ownedStockRepository.findAll();
            AiResponse aiResponse = getConsulting(stockTransactions);
            System.out.println("-------------------------------1");
            InvestmentStyle investmentStyle = InvestmentStyle.builder().childId(TEMP_CHILD_ID).childName(TEMP_CHILD_NAME).investmentStyle(aiResponse.getInvestmentStyle()).build();
            System.out.println("-------------------------------2");
            List<InvestmentArea> investmentAreas = getInvestmentArea(ownedStocks);
            System.out.println("-------------------------------3");
            InvestmentSummary investmentSummary = InvestmentSummary.builder().totalInvestmentsCount(getTotalInvestmentsCount(stockTransactions)).totalReturnRate(getTotalReturnRate(ownedStocks)).build();
            System.out.println("-------------------------------4");
            TopStock topStock = getTopStock(ownedStocks);
            System.out.println("-------------------------------5");
            BottomStock bottomStock = getBottomStock(ownedStocks);
            System.out.println("-------------------------------6");


            ReportDetail reportDetail = ReportDetail.builder()
                    .reportMonth(reportMonth.toString())
                    .investmentStyle(investmentStyle)
                    .investmentArea(investmentAreas)
                    .investmentSummary(investmentSummary)
                    .topStock(topStock)
                    .bottomStock(bottomStock)
                    .advice(aiResponse.getAdvice())
                    .build();

            Report report = toReport(reportDetail);
            reportRepository.save(report);
        }catch(Exception e){

            System.out.println(e.getMessage());
            System.out.println("============");
            e.printStackTrace();
            throw new BaseException(ErrorCode.JSON_PROCESSING_FAILED);
        }
    }

    //투자(거래) 횟수
    public int getTotalInvestmentsCount(List<StockTransaction> stockTransactions) {
        int result=0;
        for  (StockTransaction tx : stockTransactions) {
            if(tx.getTransactionType()== TransactionType.BUY) {
                result+= 1;
            }
        }
        return result;
    }

    //전체 수익률
    public BigDecimal getTotalReturnRate(List<OwnedStock> ownedStocks){

        //총 투자금액
        BigDecimal totalPurchaseAmount = ownedStocks.stream()
                .map(OwnedStock::getPurchaseAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //총 평가 손익 합계
        BigDecimal totalProfitAndLoss = ownedStocks.stream()
                .map(OwnedStock::getValuationProfitAndLoss)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // (총 손익 % 총 투자금액) * 100
        if(totalProfitAndLoss.compareTo(BigDecimal.ZERO)>0) {
            return totalProfitAndLoss
                    .divide(totalPurchaseAmount,2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO; //투자 금액이 0이면 수익률도 0
    }

    //투자 분야별 퍼센트 (IT, 에너지, 기타 등 구체적인 카테고리 분류 및 비율)
    public List<InvestmentArea> getInvestmentArea(List<OwnedStock> ownedStocks){
       List<InvestmentArea> investmentAreas = new ArrayList<>();

       if(ownedStocks == null || ownedStocks.isEmpty()){
           return new ArrayList<>();
       }

       Map<String,BigDecimal> categoryAmounts = ownedStocks.stream()
               .collect(Collectors.groupingBy(stock-> stock.getStock().getCategory(),
                       Collectors.reducing(BigDecimal.ZERO, OwnedStock::getPurchaseAmount,BigDecimal::add)
                       ));


       BigDecimal total = categoryAmounts.values().stream()
                   .reduce(BigDecimal.ZERO, BigDecimal::add);

       return categoryAmounts.entrySet().stream()
               .map(entry -> InvestmentArea.builder()
                       .category(entry.getKey())
                       .percentage(entry.getValue()
                               .divide(total,2,RoundingMode.HALF_UP)
                               .multiply(BigDecimal.valueOf(100)))
                       .build())
               .collect(Collectors.toList());
       }


    // 가장 수익률이 높은 종목 이름과 수익률 -> 동일하면 최대/최소값이 여러 개 있어도 그 중 하나만 가져옴
    public TopStock getTopStock(List<OwnedStock> ownedStocks){

        return ownedStocks.stream()
                .max(Comparator.comparing(OwnedStock::getReturnRate)
                        .thenComparing(OwnedStock::getPurchaseAmount))
                .map(stock -> TopStock.builder()
                        .stockName(stock.getStock().getName())
                        .returnRate(stock.getReturnRate())
                        .build())
                .orElse(null);
    }



    // 가장 수익률이 낮은 종목 이름과 수익률
    public BottomStock getBottomStock(List<OwnedStock> ownedStocks) {
        return ownedStocks.stream()
                .min(Comparator.comparing(OwnedStock::getReturnRate)
                        .thenComparing(OwnedStock::getPurchaseAmount))
                .map(stock -> BottomStock.builder()
                        .returnRate(stock.getReturnRate())
                        .stockName(stock.getStock().getName())
                        .build())
                .orElse(null);
    }

    // 거래 내역을 문자열 형식으로 변환하는 헬퍼 메서드
    private String formatTransactionsData(List<StockTransaction> transactions) {
        StringBuilder sb = new StringBuilder();

        for (StockTransaction tx : transactions) {
            sb.append("거래일자: ").append(tx.getRequestDate())
                    .append(", 주식ID: ").append(tx.getStock().getCode())
                    .append(", 거래타입: ").append(tx.getTransactionType())
                    .append(", 수량: ").append(tx.getQuantity())
                    .append(", 주당가격: ").append(tx.getPricePerShare())
                    .append(", 총액: ").append(tx.getAmount())
                    .append(", 상태: ").append(tx.getStatus())
                    .append("\n");
        }

        return sb.toString();
    }
}
