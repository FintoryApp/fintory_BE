package com.fintory.auth.controller;

import com.fintory.auth.dto.AuthToken;
import com.fintory.auth.dto.request.LoginRequest;
import com.fintory.auth.dto.request.ReissueRequest;
import com.fintory.auth.dto.request.SignUpRequest;
import com.fintory.auth.dto.request.SocialLoginRequest;
import com.fintory.auth.util.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Tag(name = "User API", description = "회원 관련 api")
public interface AuthController{

    @Operation(summary = "회원등록 + 자동로그인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 및 자동로그인 성공: Access와 Refresh token 반환"),
            @ApiResponse(responseCode = "400", description = "입력값이 유효하지 않습니다: [ex) email: 올바른 이메일 형식으로 입력해주세요]")
    })
    ResponseEntity<com.fintory.common.api.ApiResponse<AuthToken>> signup(
            @Parameter(description = "닉네임, 비밀번호, 이메일", required = true)
            @RequestBody
            @Valid
            SignUpRequest request
    );

    @Operation(summary = "수동 로그인", description = "email&pw용")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공: Access와 Refresh token 반환"),
            @ApiResponse(responseCode = "400", description = "아이디 또는 비밀번호가 일치하지 않습니다")
    })
    ResponseEntity<com.fintory.common.api.ApiResponse<AuthToken>> login(
            @Parameter(description = "이메일, 비밀번호", required = true)
            @RequestBody
            @Valid
            LoginRequest request
    );

    @Operation(summary = "구글 로그인")
    @ApiResponse(responseCode = "200", description = "로그인 성공: Access와 Refresh token 반환")
    ResponseEntity<com.fintory.common.api.ApiResponse<AuthToken>> socialLogin(
            @RequestBody
            SocialLoginRequest request
    );

    @Operation(summary = "로그아웃", description = "at 토큰을 헤더에 포함해서 요청해야 함, axios.get(\"/api/user\", {\n" +
            "  headers: {\n" +
            "    Authorization: `Bearer ${token}`\n" +
            "  }\n" +
            "});")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "400", description = "사용자가 로그인되어 있지 않습니다"),
            @ApiResponse(responseCode = "401", description = "토큰이 존재하지 않습니다 등 토큰 에러")
    })
    ResponseEntity<com.fintory.common.api.ApiResponse<String>> logout(
            @Parameter(description = "서버가 자동으로 꺼내옴. 인자 넣을 필요 없음")
            @AuthenticationPrincipal
            CustomUserDetails user
    );

    @Operation(summary = "이메일 중복 검사", description = "중복 여부 반환: false/true 값에 따라 프론트에서 로직을 분기해주세요")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "false면 사용가능 이메일, true면 이미 사용중인 이메일"),
            @ApiResponse(responseCode = "400", description = "이메일 형식 오류 또는 이메일이 비어있는 경우 발생하는 유효성 검증 실패")
    })
    ResponseEntity<com.fintory.common.api.ApiResponse<Boolean>> checkDuplicateEmail (
            @Parameter(description = "등록할 이메일", required = true)
            @RequestParam
            @NotBlank(message = "이메일은 필수입니다")
            @Email(message = "올바른 이메일 형식이 아닙니다")
            String email
    );

    @Operation(summary = "토큰 재발급", description = "조건에 따라 newAccess+refresh 혹은 newAccess+newRefresh 응답")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Access Token, Refresh Token 재발급 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다 등 토큰 에러")
    })
    ResponseEntity<com.fintory.common.api.ApiResponse<AuthToken>> reissue(
            @Parameter(description = "refresh token", required = true)
            @RequestBody
            @Valid
            ReissueRequest request
    );

    @Operation(summary = "현재 로그인한 계정 정보 출력", description = "at 토큰을 헤더에 포함해서 요청해야 함")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "닉네임, role(child, parent), email 반환"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다 등 토큰 에러")
    })
    ResponseEntity<com.fintory.common.api.ApiResponse<Map<String, String>>> me(
            @AuthenticationPrincipal
            CustomUserDetails user
    );

}
