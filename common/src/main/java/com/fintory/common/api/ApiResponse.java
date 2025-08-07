package com.fintory.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@AllArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class ApiResponse<T> {

    private ApiResultCode resultCode;
    private T data;
    private String message;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(ApiResultCode.SUCCESS, data, null);
    }

    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<>(ApiResultCode.SUCCESS, null, message);
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(ApiResultCode.SUCCESS, data, message);
    }

}
