package com.fintory.child.domain.attendance.controller;

import com.fintory.auth.util.CustomUserDetails;
import com.fintory.common.api.ApiResponse;
import com.fintory.domain.attendence.dto.AttendanceLogResponse;
import com.fintory.domain.attendence.dto.CheckInResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@Tag(name = "출석 체크 API")
public interface AttendanceController {

    @Operation(summary = "출석 체크 및 연속 출석일 수 반환")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "연속 출석일 반환")
    ResponseEntity<ApiResponse<CheckInResponse>> attendanceCheck(@AuthenticationPrincipal CustomUserDetails user);

    @Operation(summary = "출석일 전체 리스트 반환")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "출석 리스트 반환(LocalDate형)")
    ResponseEntity<ApiResponse<List<AttendanceLogResponse>>> getAttendanceList(@AuthenticationPrincipal CustomUserDetails user);
}
