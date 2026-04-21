package com.kano.main_data.model.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResult<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResult<T> success() {
        return ApiResult.<T>builder()
                .code(200)
                .message("Success")
                .data(null)
                .build();
    }

    public static <T> ApiResult<T> success(T data) {
        return ApiResult.<T>builder()
                .code(200)
                .message("Success")
                .data(data)
                .build();
    }

    public static <T> ApiResult<T> error(int code, String message) {
        return ApiResult.<T>builder()
                .code(code)
                .message(message)
                .data(null)
                .build();
    }

}
