package com.fintory.fintory.backend.project.consulting.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintory.fintory.backend.project.consulting.config.ChatGptConfig;
import com.fintory.fintory.backend.project.consulting.dto.ChatCompletionDto;
import com.fintory.fintory.backend.project.consulting.dto.ChatRequestMessageDto;
import com.fintory.fintory.backend.project.stock.entity.StockTransaction;
import com.fintory.fintory.backend.project.stock.service.StockTransactionService;
import io.github.flashvayne.chatgpt.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConsultingService {

    private final ChatGptConfig chatGptConfig;
    private final RestTemplate restTemplate;
    private final StockTransactionService stockTransactionService;

    @Value("${openai.url.model}")
    private String modelUrl;

    @Value("${openai.url.model-list}")
    private String modelListUrl;

    @Value("${openai.url.prompt}")
    private String promptUrl;


    // chatGPT 응답 요청 보내는 비즈니스 로직
    public Map<String, Object> prompt(ChatCompletionDto chatCompletionDto){
        Map<String, Object> result = new HashMap<>();

        HttpHeaders headers = chatGptConfig.httpHeaders();

        //데이터 꺼내오기
        String transaction = formatTransactionsData(stockTransactionService.getStockTransactions());

        log.info("Prompt transaction: " + transaction);

        // transaction 필드에 넣는 대신, message content에 추가하기
        if (chatCompletionDto.getMessages() != null && !chatCompletionDto.getMessages().isEmpty()) {
            ChatRequestMessageDto firstMessage = chatCompletionDto.getMessages().get(0);
            String newContent = firstMessage.getContent() + "\n\n거래 데이터:\n" + transaction;
            firstMessage.setContent(newContent);
        }

        HttpEntity<ChatCompletionDto> requestEntity = new HttpEntity<>(chatCompletionDto, headers);
        ResponseEntity<String> response = chatGptConfig
                .restTemplate()
                .exchange(promptUrl,HttpMethod.POST, requestEntity,String.class);
        try{
            ObjectMapper mapper = new ObjectMapper();
            result = mapper.readValue(response.getBody(), new TypeReference<>(){}); //TypeReference<>로 제너릭 타입을 유지 -> 안 쓰면 내부 타입이 무시됨.

        }catch(JsonProcessingException e){
            log.debug("JsonProcessingException " + e.getMessage());
        }catch(RuntimeException e){
            log.debug("RuntimeException " + e.getMessage());
        }
        return result;
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
