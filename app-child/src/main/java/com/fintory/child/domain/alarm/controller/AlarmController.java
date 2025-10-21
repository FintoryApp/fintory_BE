package com.fintory.child.domain.alarm.controller;

import com.fintory.auth.util.CustomUserDetails;
import com.fintory.common.api.ApiResponse;
import com.fintory.domain.alarm.dto.FcmTokenRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name="알림 API")
public interface AlarmController {

    @Operation(summary="백엔드에게 FCM 토큰 전달")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",description = "FCM 토큰 전달")
    ResponseEntity<ApiResponse<Void>> saveToken(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody FcmTokenRequest request);
    
    @Operation(summary = "로그아웃 시 FCM 토큰 삭제")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",description = "FCM 토큰 삭제")
    ResponseEntity<ApiResponse<Void>> deleteToken(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody FcmTokenRequest request);
}
