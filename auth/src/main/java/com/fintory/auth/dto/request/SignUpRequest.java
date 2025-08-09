package com.fintory.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpRequest(

        @NotBlank(message = "닉네임은 필수 입력 항목입니다")
        @Size(min = 5, max = 10, message = "닉네임는 5~10자로 입력해주세요")
        String nickname,

        @NotBlank(message = "비밀번호는 필수 입력 항목입니다")
        @Size(min = 6, max = 12, message = "비밀번호는 6~12자로 입력해주세요")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]{6,12}$", message = "비밀번호는 영문과 숫자를 포함해야 합니다")
        String password,

        @NotBlank(message = "이메일은 필수 입력 항목입니다")
        @Email(message = "올바른 이메일 형식으로 입력해주세요")
        String email


) {
}
