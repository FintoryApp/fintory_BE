package com.fintory.fintory.backend.project.consulting.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintory.fintory.backend.project.consulting.config.ChatGptConfig;
import com.fintory.fintory.backend.project.consulting.dto.ChatCompletionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConsultService {

    private final ChatGptConfig chatGptConfig;
    private final RestTemplate restTemplate;

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
}
