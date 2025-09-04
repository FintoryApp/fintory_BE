package com.fintory.child.domain.attendance.controller;

import com.fintory.auth.util.CustomUserDetails;
import com.fintory.common.api.ApiResponse;
import com.fintory.domain.attendence.dto.AttendanceLogResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

public interface AttendanceController {

    ResponseEntity<ApiResponse<Integer>> attendanceCheck(@AuthenticationPrincipal CustomUserDetails user);
    ResponseEntity<ApiResponse<List<AttendanceLogResponse>>> getAttendanceList();
}
