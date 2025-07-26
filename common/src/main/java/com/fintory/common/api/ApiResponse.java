package com.fintory.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.fintory.common.api.ApiResultCode.SUCCESS;

@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@AllArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class ApiResponse<T> {

    private ApiResultCode resultCode;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(SUCCESS, data);
    }

    public static <T> ApiResponse<T> okWithNoData(T data) {
        return new ApiResponse<>(SUCCESS, null);
    }

}
