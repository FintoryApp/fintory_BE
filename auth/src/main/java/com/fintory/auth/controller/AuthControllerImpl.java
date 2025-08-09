package com.fintory.auth.controller;

import com.fintory.auth.dto.AuthToken;
import com.fintory.auth.dto.request.LoginRequest;
import com.fintory.auth.dto.request.ReissueRequest;
import com.fintory.auth.dto.request.SignUpRequest;
import com.fintory.auth.service.AuthService;
import com.fintory.auth.util.CustomUserDetails;
import com.fintory.common.api.ApiResponse;
import com.fintory.common.exception.DomainErrorCode;
import com.fintory.common.exception.DomainException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthControllerImpl implements AuthController{

    private final AuthService authService;

    @Override
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthToken>> signup(@RequestBody @Valid SignUpRequest request) {
        log.info("회원가입 요청: {}", request.email());
        AuthToken token = authService.signup(request);
        return ResponseEntity.ok(ApiResponse.ok(token, "회원가입 및 자동로그인 성공"));
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthToken>> login(@RequestBody @Valid LoginRequest request) {
        log.info("로그인 요청: {}", request.email());
        AuthToken token = authService.login(request.email(), request.password());
        log.info("로그인 성공");
        return ResponseEntity.ok(ApiResponse.ok(token, "로그인 성공"));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@AuthenticationPrincipal CustomUserDetails user) {
        String email = user.getUsername(); // 로그인된 사용자의 이메일 반환
        log.info("로그아웃할 계정(로그인 되어있는 계정): {}", email);
        authService.logout(email);
        return ResponseEntity.ok(ApiResponse.ok("로그아웃 성공"));
    }

    @Override
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkDuplicateEmail(
            @RequestParam
            @NotBlank(message = "이메일은 필수입니다")
            @Email(message = "올바른 이메일 형식이 아닙니다")
            String email
    ) {
        boolean isDuplicate = authService.checkDuplicateEmail(email);
        return ResponseEntity.ok(ApiResponse.ok(isDuplicate));
    }


    /**
     * at 만료시 rt로 at 재발급 요청 API
     * @param request 토큰 재발급 요청 DTO (refreshToken)
     * @return 새 at 와 기존 rt를 포함하는 응답, rt 만료시간이 기준을 넘으면 새 rt도 at 랑 같이 묶어서 응답
     */
    @Override
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<AuthToken>> reissue(@RequestBody @Valid ReissueRequest request) {
        log.info("토큰 재발급 요청");
        AuthToken token = authService.reissue(request.refreshToken());
        if (token.refreshToken() != null) {
            return ResponseEntity.ok(ApiResponse.ok(token, "Access Token, Refresh Token 재발급 성공"));
        }
        return ResponseEntity.ok(ApiResponse.ok(token, "Access Token 재발급 성공"));
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, String>>> me(@AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) {
            throw new DomainException(DomainErrorCode.LOGINED_USER_NOT_FOUND);
        }
        log.info("user: {}", user.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "email", user.getUsername(),
                "nickname", user.getNickname(),
                "role", user.getRole()
        )));
    }

}