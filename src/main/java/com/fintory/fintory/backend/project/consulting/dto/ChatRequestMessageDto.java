package com.fintory.fintory.backend.project.consulting.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor(access= AccessLevel.PROTECTED)
public class ChatRequestMessageDto {

    private String role;
    private String content;

    @Builder
    public ChatRequestMessageDto(String role, String content) {
        this.role = role;
        this.content = content;
    }
}
