package com.fintory.infra.domain.consulting.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.child.model.Child;
import com.fintory.domain.consulting.dto.*;
import com.fintory.domain.consulting.model.Report;
import com.fintory.domain.consulting.service.ConsultingService;
import com.fintory.domain.portfolio.model.OwnedStock;
import com.fintory.domain.portfolio.model.StockTransaction;
import com.fintory.domain.portfolio.model.TransactionType;
import com.fintory.domain.stock.model.LiveStockPrice;
import com.fintory.domain.portfolio.service.ExchangeRateService;
import com.fintory.infra.domain.consulting.repository.ReportRepository;
import com.fintory.infra.domain.portfolio.repository.OwnedStockRepository;
import com.fintory.infra.domain.stock.repository.LiveStockPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



import static com.fintory.domain.consulting.dto.ReportDetail.toReport;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultingServiceImpl implements ConsultingService {


    //private final ChatClient chatClient;
    private final ReportRepository reportRepository;
    private final OwnedStockRepository ownedStockRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final LiveStockPriceRepository liveStockPriceRepository;
    private final ExchangeRateService exchangeRateService;


    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Override
    public AiResponse getConsulting(List<StockTransaction> stockTransactions) {

            //거래 내역 문자열로 변환
            String transactionData = formatTransactionsData(stockTransactions);

            log.info("Consulting transaction data: {}", transactionData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = Map.of(
                    "model", "gpt-3.5-turbo",
                    "messages", List.of(
                            Map.of("role", "system", "content", """
                        당신은 주식 트레이딩 전문가입니다.
                        반드시 다음 JSON 형식으로만 응답하세요. 다른 텍스트는 포함하지 마세요.
                        {
                            "investmentStyle": "공격형 또는 중립형 또는 안정형",
                            "advice": "투자 조언 메시지"
                        }
                        """),
                            Map.of("role", "user", "content", """
                        다음 거래 내역을 분석해주세요:
                        %s
                        
                        위 거래 내역을 바탕으로 다음을 분석하여 JSON 형식으로만 응답해주세요:
                        1. 투자 성향 (공격형, 중립형, 안정형 중 하나)
                        2. 향후 전략 추천 메시지
                        """.formatted(transactionData))
                    ),
                    "max_tokens", 200,
                    "temperature", 0.7
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody,headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.openai.com/v1/chat/completions",
                    request,
                    Map.class
            );

            return parseOpenAiResponse(response.getBody());
            /*
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


            return objectMapper.readValue(response, AiResponse.class);

             */
    }

    //조회 메서드
    @Override
    @Transactional
    public ReportDetail getConsultingByDate(String date, Child child)  {
        try {
            Report report = reportRepository.findByReportMonthAndChild(date,child).orElseThrow(() -> new DomainException(DomainErrorCode.REPORT_NOT_FOUND));
            TradingReport tradingReportDto = objectMapper.readValue(
                    report.getReportJson(),
                    TradingReport.class
            );

            return ReportDetail.from(report, tradingReportDto);
        } catch (JsonProcessingException e) {
            throw new DomainException(DomainErrorCode.JSON_PARSING_ERROR);
        }
    }


    //REVIEW 현재는 거래내역과 보유주식 없으면 리포트 생성X,
    //생성 + 저장 통합 로직
    @Override
    public void saveReportDetail(List<StockTransaction> stockTransactions,Child child){
        try {
            YearMonth reportMonth = YearMonth.now();

            // Child의 보유주식 조회
            List<OwnedStock> ownedStocks = ownedStockRepository.findByAccount(child.getAccount());

            // 거래내역과 보유주식 둘 다 없으면 리포트 생성x
            if ((stockTransactions == null || stockTransactions.isEmpty()) &&
                    (ownedStocks == null || ownedStocks.isEmpty())) {
                log.info("보유 주식, 거래내역이 존재하지 않음", child.getId());
                return; // 스킵
            }

            // 거래내역이 없어도 보유주식이 있으면 리포트 생성(투자횟수 0 )
            //보유주식이 없으면 모든 값들이 0으로 보임(수익률, topstock, bottomstock)
            AiResponse aiResponse;
            if (stockTransactions != null && !stockTransactions.isEmpty()) {
                aiResponse = getConsulting(stockTransactions);
            } else {
                // 거래내역이 없으면 기본 응답
                aiResponse = createDefaultResponse();
            }

            InvestmentStyle investmentStyle = InvestmentStyle.builder()
                    .childId(child.getId())
                    .childName(child.getNickname())
                    .investmentStyle(aiResponse.getInvestmentStyle())
                    .build();

            List<InvestmentArea> investmentAreas = getInvestmentArea(ownedStocks);

            InvestmentSummary investmentSummary = InvestmentSummary.builder()
                    .totalInvestmentsCount(getTotalInvestmentsCount(stockTransactions))
                    .totalReturnRate(getTotalReturnRate(ownedStocks))
                    .build();

            TopStock topStock = getTopStock(ownedStocks);
            BottomStock bottomStock = getBottomStock(ownedStocks);

            ReportDetail reportDetail = ReportDetail.builder()
                    .reportMonth(reportMonth.toString())
                    .investmentStyle(investmentStyle)
                    .investmentArea(investmentAreas)
                    .investmentSummary(investmentSummary)
                    .topStock(topStock)
                    .bottomStock(bottomStock)
                    .advice(aiResponse.getAdvice())
                    .build();

            TradingReport tradingReport = TradingReport.fromReportDetail(reportDetail);
            String reportJson = objectMapper.writeValueAsString(tradingReport);

            Report report = toReport(reportDetail, reportJson,child);

            reportRepository.save(report);
        }catch(Exception e){

            throw new DomainException(DomainErrorCode.JSON_PARSING_ERROR);
        }
    }

    private AiResponse parseOpenAiResponse(Map<String, Object> responseBody) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");

            log.info("OpenAI 원본 응답: {}", content);

            // JSON 추출 및 정리
            String cleanedJson = extractAndCleanJson(content);

            return objectMapper.readValue(cleanedJson , AiResponse.class);
        }catch(JsonProcessingException e){
            log.error("OpenAI 응답 파싱 실패: {}", e.getMessage());
            return createDefaultResponse();
        }
    }

    //투자(거래) 횟수
    private int getTotalInvestmentsCount(List<StockTransaction> stockTransactions) {
        if(stockTransactions == null || stockTransactions.isEmpty()) {
            return 0;
        }

        int result=0;
        for  (StockTransaction tx : stockTransactions) {
            if(tx.getTransactionType()== TransactionType.BUY) {
                result+= 1;
            }
        }
        return result;
    }

    //전체 수익률
    private BigDecimal getTotalReturnRate(List<OwnedStock> ownedStocks){

        if (ownedStocks == null || ownedStocks.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalPurchaseAmount = BigDecimal.ZERO;
        BigDecimal totalCurrentValue = BigDecimal.ZERO;
        BigDecimal exchangeRate = exchangeRateService.getExchangeRate();

        for(OwnedStock ownedStock : ownedStocks) {
            //현재가 조회 -> 매달 10시에 리포트 생성
            LiveStockPrice liveStockPrice = liveStockPriceRepository.findByStock(ownedStock.getStock())
                    .orElse(null);

            if(liveStockPrice != null) {
                BigDecimal currentPrice = liveStockPrice.getCurrentPrice();

                // 해외주식이면 환율 적용
                if("USD".equals(ownedStock.getStock().getCurrencyName())) {
                    currentPrice = currentPrice.multiply(exchangeRate);
                }

                BigDecimal currentValue = currentPrice.multiply(ownedStock.getQuantity());
                totalCurrentValue = totalCurrentValue.add(currentValue);
            }
            totalPurchaseAmount = totalPurchaseAmount.add(ownedStock.getPurchaseAmount());
        }

        if (totalPurchaseAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal profitLoss = totalCurrentValue.subtract(totalPurchaseAmount);
        return profitLoss.divide(totalPurchaseAmount, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }



    //투자 분야별 퍼센트 (IT, 에너지, 기타 등 구체적인 카테고리 분류 및 비율)
    private List<InvestmentArea> getInvestmentArea(List<OwnedStock> ownedStocks){
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
    private TopStock getTopStock(List<OwnedStock> ownedStocks) {
        if (ownedStocks == null || ownedStocks.isEmpty()) {
            return null;
        }

        TopStock bestStock = null;
        BigDecimal maxReturnRate = new BigDecimal("-999999");
        BigDecimal exchangeRate = exchangeRateService.getExchangeRate();

        for (OwnedStock ownedStock : ownedStocks) {
            LiveStockPrice livePrice = liveStockPriceRepository
                    .findByStock(ownedStock.getStock())
                    .orElse(null);

            if (livePrice != null && ownedStock.getPurchaseAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal currentPrice = livePrice.getCurrentPrice();

                // 해외주식이면 환율 적용
                if("USD".equals(ownedStock.getStock().getCurrencyName())) {
                    currentPrice = currentPrice.multiply(exchangeRate);
                }

                BigDecimal currentValue = currentPrice.multiply(ownedStock.getQuantity());
                BigDecimal profitLoss = currentValue.subtract(ownedStock.getPurchaseAmount());
                BigDecimal returnRate = profitLoss.divide(ownedStock.getPurchaseAmount(), 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

                if (returnRate.compareTo(maxReturnRate) > 0) {
                    maxReturnRate = returnRate;
                    bestStock = TopStock.builder()
                            .stockName(ownedStock.getStock().getName())
                            .returnRate(returnRate)
                            .build();
                }
            }
        }

        return bestStock;
    }

    // 가장 수익률이 낮은 종목 이름과 수익률
    private BottomStock getBottomStock(List<OwnedStock> ownedStocks) {
        if (ownedStocks == null || ownedStocks.isEmpty()) {
            return null;
        }

        BottomStock worstStock = null;
        BigDecimal minReturnRate = new BigDecimal("999999"); // 매우 높은 값으로 초기화
        BigDecimal exchangeRate = exchangeRateService.getExchangeRate();


        for (OwnedStock ownedStock : ownedStocks) {
            LiveStockPrice livePrice = liveStockPriceRepository
                    .findByStock(ownedStock.getStock())
                    .orElse(null);

            if (livePrice != null && ownedStock.getPurchaseAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal currentPrice = livePrice.getCurrentPrice();

                // 해외주식이면 환율 적용
                if("USD".equals(ownedStock.getStock().getCurrencyName())) {
                    currentPrice = currentPrice.multiply(exchangeRate);
                }

                BigDecimal currentValue = currentPrice.multiply(ownedStock.getQuantity());
                BigDecimal profitLoss = currentValue.subtract(ownedStock.getPurchaseAmount());
                BigDecimal returnRate = profitLoss.divide(ownedStock.getPurchaseAmount(), 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

                if (returnRate.compareTo(minReturnRate) < 0) {
                    minReturnRate = returnRate;
                    worstStock = BottomStock.builder()
                            .stockName(ownedStock.getStock().getName())
                            .returnRate(returnRate)
                            .build();
                }
            }
        }

        return worstStock;
    }

    // 거래 내역을 문자열 형식으로 변환하는 헬퍼 메서드
    private String formatTransactionsData(List<StockTransaction> transactions) {
        StringBuilder sb = new StringBuilder();

        for (StockTransaction tx : transactions) {
            sb.append("거래일자: ").append(tx.getExecutedAt())
                    .append(", 주식ID: ").append(tx.getStock().getCode())
                    .append(", 거래타입: ").append(tx.getTransactionType())
                    .append(", 수량: ").append(tx.getQuantity())
                    .append(", 주당가격: ").append(tx.getPricePerShare())
                    .append(", 총액: ").append(tx.getAmount())
                    .append("\n");
        }

        return sb.toString();
    }


    private String extractAndCleanJson(String content) {
        String trimmed = content.trim();
        int startIndex = trimmed.indexOf("{");
        int endIndex = trimmed.lastIndexOf("}");

        if(startIndex==-1 || endIndex==-1 || endIndex<=startIndex){
            log.warn("유효한 JSON을 찾을 수 없습니다.");
            throw new DomainException(DomainErrorCode.JSON_PARSING_ERROR);
        }

        String jsonPart = trimmed.substring(startIndex, endIndex+1);

        if(!jsonPart.contains("investmentStyle") || !jsonPart.contains("advice")){
            log.warn("필수 필드가 없습니다.");
            throw new DomainException(DomainErrorCode.JSON_PARSING_ERROR);
        }

        return jsonPart;
    }

    //응답을 못받았을 시 기본 응답
    private AiResponse createDefaultResponse() {
        return AiResponse.builder()
                .investmentStyle("중립형")
                .advice("거래 패턴을 바탕으로 균형잡힌 투자 전략을 권장합니다.")
                .build();
    }

    // 거래내역이 없을 때 사용할 기본 응답
    private AiResponse createDefaultResponseForNoTransactions() {
        return AiResponse.builder()
                .investmentStyle("안정형")
                .advice("이번 달에는 거래가 없었습니다. 현재 보유 중인 종목들의 수익률을 확인하고 장기 투자 관점에서 포트폴리오를 점검해보세요.")
                .build();
    }



}
