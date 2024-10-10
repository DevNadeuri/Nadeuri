package com.example.Nadeuri.common.response;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final String statusMessage;
    private final T data;

    public ApiResponse(
            final String statusMessage,
            final T data
    ) {
        this.statusMessage = statusMessage;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(final T data) {
        return new ApiResponse<>(
                "성공",
                data
        );
    }

}
