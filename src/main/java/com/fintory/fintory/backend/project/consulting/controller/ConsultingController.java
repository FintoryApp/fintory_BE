package com.fintory.fintory.backend.project.consulting.controller;

import com.fintory.fintory.backend.project.consulting.dto.ChatCompletionDto;
import com.fintory.fintory.backend.project.consulting.service.ConsultService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/fintory/consulting")
public class ConsultingController {

    @Autowired
    ConsultService consultService;

    @PostMapping("/prompt")
    public ResponseEntity<Map<String,Object>> prompt(@RequestBody ChatCompletionDto chatCompletionDto){
        Map<String,Object> result = consultService.prompt(chatCompletionDto);
        return new ResponseEntity<>(result,HttpStatus.OK);
    }

}
