package com.common.web;

import com.common.error.ErrorCode;

import java.time.OffsetDateTime;

public final class ApiResponse<T> {
    private final boolean success;
    private final String message;
    private final String path;
    private final OffsetDateTime timestamp;
    private final T data;

    private ApiResponse(boolean success, String message, String path, T data) {
        this.success = success;
        this.message = message;
        this.path = path;
        this.timestamp = OffsetDateTime.now();
        this.data = data;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "ok", null, data);
    }

    public static <T> ApiResponse<T> ok(String message, String path, T data) {
        return new ApiResponse<>(true, message, path, data);
    }

    public static <T> ApiResponse<T> created(String message, String path, T data) {
        return new ApiResponse<>(true, message, path, data);
    }

    public static <T> ApiResponse<T> error(ErrorCode code, String message, String path) {
        return new ApiResponse<>(false, code.name() + ": " + message, path, null);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public OffsetDateTime getTimestamp() { return timestamp; }
    public T getData() { return data; }
}
