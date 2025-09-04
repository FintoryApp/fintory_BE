package com.fintory.child.domain.attendance.controller;

import com.fintory.auth.util.CustomUserDetails;
import com.fintory.common.api.ApiResponse;
import com.fintory.domain.attendence.dto.AttendanceLogResponse;
import com.fintory.domain.attendence.service.AttendanceService;
import com.fintory.domain.child.model.Child;
import com.fintory.domain.child.service.ChildService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/child/attendance")
public class AttendanceControllerImpl implements AttendanceController{

    private final AttendanceService attendanceService;
    private final ChildService childService;

    @Override
    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<Integer>> attendanceCheck(@AuthenticationPrincipal CustomUserDetails user) {
        Child child = childService.getChild(user.getUsername());
        int response = attendanceService.check(child); //연속 출석일 리턴
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Override
    @GetMapping("/attendance-log")
    public ResponseEntity<ApiResponse<List<AttendanceLogResponse>>> getAttendanceList(@AuthenticationPrincipal CustomUserDetails user) {
        return null;
    }
}
