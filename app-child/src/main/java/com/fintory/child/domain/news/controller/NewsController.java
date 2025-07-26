package com.fintory.child.domain.news.controller;

import com.fintory.domain.news.dto.NewsResponse;
import com.fintory.domain.news.dto.NewsSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "뉴스 API", description = "뉴스 조회 및 크롤링 관련 API")
public interface NewsController {

    @Operation(summary = "뉴스 상세 조회", description = "특정 ID의 뉴스 상세 정보를 조회")
    @ApiResponse(responseCode = "200", description = "뉴스 조회 성공: contents는 쉼표를 기준으로 다른 문단입니다")
    ResponseEntity<com.fintory.common.api.ApiResponse<NewsResponse>> getNews(
        @Parameter(description = "뉴스 ID", required = true, example = "1") Long id
    );

    @Operation(summary = "뉴스 목록 조회", description = "전체 뉴스 목록을 조회")
    @ApiResponse(responseCode = "200", description = "뉴스 목록 조회 성공")
    ResponseEntity<com.fintory.common.api.ApiResponse<List<NewsSummaryResponse>>> getNewsList();

    @Operation(summary = "뉴스 크롤링 실행", description = "최신 뉴스를 크롤링하여 저장. 응답 값은 없고 db에서 row 확인하시면 됩니다")
    @ApiResponse(responseCode = "200", description = "크롤링 성공")
    ResponseEntity<com.fintory.common.api.ApiResponse<Void>> triggerCrawling();
}