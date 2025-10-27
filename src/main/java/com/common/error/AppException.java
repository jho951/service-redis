package com.common.error;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
	
    private final ErrorCode errorCode;
    private final String detail;

    public AppException(ErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
        this.detail = detail;
    }
}
