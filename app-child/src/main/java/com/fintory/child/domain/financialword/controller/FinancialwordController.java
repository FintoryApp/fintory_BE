package com.fintory.child.domain.financialword.controller;

import com.fintory.common.api.ApiResponse;
import com.fintory.domain.financialword.dto.WordResponse;
import com.fintory.domain.financialword.dto.WordSummaryResponse;
import com.fintory.domain.financialword.dto.WordTitleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "경제 용어 API")
public interface FinancialwordController {

    @Operation(summary = "용어 상세 조회")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "용어 상세 조회 결과 반환")
    ResponseEntity<ApiResponse<WordResponse>> getWord(@PathVariable Long id);

    @Operation(summary = "용어 전체 목록 조회")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "용어 전체 목록 조회 결과 반환")
    ResponseEntity<ApiResponse<List<WordTitleResponse>>> getWordList();

    @Operation(summary = "오늘의 용어 (랜덤) 미리보기 조회")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "랜덤 단어 이름, id 반환")
    ResponseEntity<ApiResponse<WordSummaryResponse>> getRandomWord();

    // 챗봇 형태로 다른 페이지에서도 즉석으로 호출해도 괜찮을 지도?
    @Operation(summary = "용어 검색")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "검색된 모든 단어 리스트 반환")
    ResponseEntity<ApiResponse<List<WordTitleResponse>>> searchWord(
            @Parameter(description = "검색할 키워드. 한 글자도 가능", required = true)
            @RequestParam
            @Size(min = 1, max = 20, message = "1~20자 이내로 검색해주세요")
            String keyword
    );
}
