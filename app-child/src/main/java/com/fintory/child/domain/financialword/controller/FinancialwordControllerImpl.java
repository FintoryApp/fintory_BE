package com.fintory.child.domain.financialword.controller;

import com.fintory.common.api.ApiResponse;
import com.fintory.domain.financialword.dto.WordResponse;
import com.fintory.domain.financialword.dto.WordSummaryResponse;
import com.fintory.domain.financialword.dto.WordTitleResponse;
import com.fintory.domain.financialword.service.WordService;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/child/financialword")
@RequiredArgsConstructor
public class FinancialwordControllerImpl implements FinancialwordController{

    private final WordService wordService;

    @Override
    @GetMapping("/get-word/{id}")
    public ResponseEntity<ApiResponse<WordResponse>> getWord(@PathVariable Long id) {
        WordResponse response = wordService.getWordById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Override
    @GetMapping("/get-word-list")
    public ResponseEntity<ApiResponse<List<WordTitleResponse>>> getWordList() {
        List<WordTitleResponse> words = wordService.getWordList();
        return ResponseEntity.ok(ApiResponse.ok(words));
    }

    @Override
    @GetMapping("/get-random-word")
    public ResponseEntity<ApiResponse<WordSummaryResponse>> getRandomWord() {
        WordSummaryResponse summary = wordService.getRandomWordSummary();
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @Override
    @GetMapping("/get-search-word-list")
    public ResponseEntity<ApiResponse<List<WordTitleResponse>>> searchWord(
            @RequestParam
            @Size(min = 1, max = 20, message = "1~20자 이내로 검색해주세요")
            String keyword
    ) {
        List<WordTitleResponse> searchList = wordService.getSearchWordList(keyword);
        return ResponseEntity.ok(ApiResponse.ok(searchList));
    }


}
