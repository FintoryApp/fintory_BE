package com.fintory.child.domain.consulting.controller;

import com.fintory.auth.util.CustomUserDetails;
import com.fintory.common.api.ApiResponse;
import com.fintory.domain.consulting.dto.ReportDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
@Tag(name = "컨설팅 리포트 API")
public interface ConsultingController {

    @Operation(summary = "테스트용 컨설팅 리포트 생성 트리거")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "컨설팅 리포트 생성 완료")
    void triggerConsultingReport();

    @Operation(summary = "날짜별 컨설팅 리포트 조회")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "컨설팅 리포트 상세 정보 반환")
    ResponseEntity<ApiResponse<ReportDetail>> getConsultingByDate(
            @Parameter(description = "리포트 월 (예: 2024-01)", required = true)
            @PathVariable String reportMonth,
            @AuthenticationPrincipal CustomUserDetails user
    );
}
