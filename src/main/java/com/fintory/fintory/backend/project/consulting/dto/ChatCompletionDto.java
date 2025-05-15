package com.fintory.fintory.backend.project.consulting.dto;

import lombok.*;

import java.util.List;

@Getter
@ToString
@Setter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
public class ChatCompletionDto {

    //사용할 모델
    private String model;

    private List<ChatRequestMessageDto> messages;


    @Builder
    public ChatCompletionDto(String model, List<ChatRequestMessageDto> messages){
        this.model = model;
        this.messages = messages;
    }
}
