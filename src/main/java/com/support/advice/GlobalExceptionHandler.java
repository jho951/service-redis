package com.support.advice;

import com.common.error.AppException;
import com.common.error.ErrorCode;
import com.common.web.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleApp(AppException e, HttpServletRequest req) {
        ErrorCode code = e.getErrorCode();
        return ResponseEntity
                .status(code.httpStatus())
                .body(ApiResponse.error(code, e.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAny(Exception e, HttpServletRequest req) {
        ErrorCode code = ErrorCode.INTERNAL;
        return ResponseEntity
                .status(code.httpStatus())
                .body(ApiResponse.error(code, e.getMessage(), req.getRequestURI()));
    }
}
