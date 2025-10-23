package com.fintory.child.domain.alarm.controller;

import com.fintory.auth.util.CustomUserDetails;
import com.fintory.common.api.ApiResponse;
import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import com.fintory.domain.alarm.dto.FcmTokenRequest;
import com.fintory.domain.alarm.dto.AlarmStatusRequest;
import com.fintory.domain.alarm.service.AlarmService;
import com.fintory.domain.child.model.Child;
import com.fintory.infra.domain.child.repository.ChildRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/child/alarm")
@RequiredArgsConstructor
public class AlarmControllerImpl {

    private final AlarmService alarmService;
    private final ChildRepository childRepository;

    @PostMapping("/token")
    public ResponseEntity<ApiResponse<Void>> saveToken(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody FcmTokenRequest request){
        Child child = childRepository.findByEmail(userDetails.getUsername()).orElseThrow(()-> new DomainException(DomainErrorCode.USER_NOT_FOUND));
        alarmService.saveToken(child,request.token());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // Controller
    @DeleteMapping("/token")
    public ResponseEntity<ApiResponse<Void>> deleteToken(@RequestBody FcmTokenRequest request) {
        alarmService.deleteToken(request.token());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/status")
    public ResponseEntity<ApiResponse<Void>> setStatus(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody AlarmStatusRequest request){
        Child child = childRepository.findByEmail(userDetails.getUsername()).orElseThrow(()-> new DomainException(DomainErrorCode.USER_NOT_FOUND));
        alarmService.setStatus(child,request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
