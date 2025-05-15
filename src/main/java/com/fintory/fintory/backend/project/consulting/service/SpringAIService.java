package com.fintory.fintory.backend.project.consulting.service;

import com.fintory.fintory.backend.project.stock.entity.StockTransaction;
import com.fintory.fintory.backend.project.stock.service.StockTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.stringtemplate.v4.AutoIndentWriter;

import java.util.List;

/* Spring AI Framework 사용한 open ai 연동 방법*/

@Slf4j
@Service
public class SpringAIService {
    private ChatClient chatClient;
    private final StockTransactionService stockTransactionService;

    public SpringAIService(ChatClient.Builder builder, StockTransactionService stockTransactionService) {
        this.chatClient = builder.build();
        this.stockTransactionService = stockTransactionService;
    }

    public String consult(){

        //거래 내역 데이터 가져오기
        List<StockTransaction> transactions = stockTransactionService.getStockTransactions();

        //거래 내역 문자열로 변환
        String transactionData = formatTransactionsData(transactions);

        log.info("Consulting transaction data: {}", transactionData);
        return chatClient.prompt()
                .system("당신은 주식 트레이딩 전문가입니다. 제공된 거래 내역을 분석하여 다음 정보를 제공하세요: "
                        +"1) 주요 패턴 2) 수익률 계산 3) 투자 성향(공격형,중립형, 안정형)을 분류해주고 4) 향후 전략 추천. "
                        +"전문적이고 간결한 언어로 응답하세요.")
                .user(u->u
                        .text("다음 주식 거래 내역을 분석해주세요:\n" + transactionData)) // 여기에 데이터 추가
                .options(OpenAiChatOptions.builder()
                        .model("gpt-4")
                        .temperature(0.7)
                        .maxTokens(1000) // 토큰 수 증가
                        .build())
                .call()
                .content();
    }
    // 거래 내역을 문자열 형식으로 변환하는 헬퍼 메서드
    private String formatTransactionsData(List<StockTransaction> transactions) {
        StringBuilder sb = new StringBuilder();

        for (StockTransaction tx : transactions) {
            sb.append("거래일자: ").append(tx.getRequestDate())
                    .append(", 주식ID: ").append(tx.getStockId())
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
