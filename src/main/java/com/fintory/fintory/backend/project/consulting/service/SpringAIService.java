package com.fintory.fintory.backend.project.consulting.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

/* Spring AI Framework 사용한 open ai 연동 방법*/

@Service
public class SpringAIService {
    private final ChatClient chatClient;

    public SpringAIService(ChatClient.Builder builder){
        this.chatClient = builder.build();
    }

    public String consult(){
        return chatClient.prompt()
                .system("당신은 주식 트레이딩 전문가입니다. 제공된 거래 내역을 분석하여 다음 정보를 제공하세요: "
                 +"1) 주요 패턴 2) 수익률 계산 3) 개선 가능한 점 4) 향후 전략 추천. "
                 +"전문적이고 간결한 언어로 응답하세요.")
                .user(u->u
                        .text("다음 주식 거래 내역을 분석해주세요:\n")) //여기에 주식 거래 내역 데이터 넣기
                .options(OpenAiChatOptions.builder()
                        .model("gpt-4")
                        .temperature(0.7) //높을 수록 더 창의적인 응답
                        .maxTokens(40)
                        .build())
                .call()
                .content();
    }
}
