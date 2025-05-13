package com.fintory.fintory.backend.project.consulting.controller;

import com.fintory.fintory.backend.project.consulting.service.SpringAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/* Spring AI Framework 사용한 OPEN AI 연동 방법*/

@RestController
public class SpringAIController {

    @Autowired
    private SpringAIService springAIService;

    @GetMapping("/ai")
    // String말고 다른 형태 연구하기
    String generation(){
        return springAIService.consult();
    }
}
