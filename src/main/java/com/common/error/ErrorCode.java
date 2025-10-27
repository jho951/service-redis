package com.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 표준화된 오류 코드 정의
 * - status: HTTP 상태 코드
 * - code:   클라이언트/로그에서 식별할 안정적 코드(대문자_스네이크)
 * - messageKey: 기본 메시지 또는 i18n 키
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ── 공통/HTTP 레벨
    VALIDATION(HttpStatus.BAD_REQUEST, "VALIDATION", "validation_error"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "bad_request"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "forbidden"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "resource_not_found"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "method_not_allowed"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", "unsupported_media_type"),
    PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "PAYLOAD_TOO_LARGE", "payload_too_large"),
    CONFLICT(HttpStatus.CONFLICT, "CONFLICT", "conflict"),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED", "too_many_requests"),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", "service_unavailable"),
    INTERNAL(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL", "internal_error"),

    // ── 데이터/영속성
    DUPLICATE_KEY(HttpStatus.CONFLICT, "DUPLICATE_KEY", "duplicate_key"),
    OPTIMISTIC_LOCK(HttpStatus.CONFLICT, "OPTIMISTIC_LOCK", "version_conflict"),

    // ── 요청 파싱/바인딩
    JSON_PARSE(HttpStatus.BAD_REQUEST, "JSON_PARSE", "json_parse_error"),
    BINDING(HttpStatus.BAD_REQUEST, "BINDING", "binding_error"),
    PATH_VARIABLE(HttpStatus.BAD_REQUEST, "PATH_VARIABLE", "path_variable_error"),
    QUERY_PARAM(HttpStatus.BAD_REQUEST, "QUERY_PARAM", "query_param_error"),

    // ── 도메인: 드로잉/도형
    DRAWING_NOT_FOUND(HttpStatus.NOT_FOUND, "DRAWING_NOT_FOUND", "drawing_not_found"),
    DRAWING_DELETED(HttpStatus.GONE, "DRAWING_DELETED", "drawing_deleted"),
    DRAWING_NOT_DELETED(HttpStatus.BAD_REQUEST, "DRAWING_NOT_DELETED", "drawing_not_deleted"),
    INVALID_SHAPE(HttpStatus.BAD_REQUEST, "INVALID_SHAPE", "invalid_shape"),
    DUPLICATED_TITLE(HttpStatus.BAD_REQUEST, "DUPLICATED_TITLE", "duplicated_title"),
    VERSION_CONFLICT(HttpStatus.CONFLICT, "VERSION_CONFLICT", "version_conflict");

    private final HttpStatus status;
    private final String code;
    private final String messageKey;


    public HttpStatus httpStatus() {
        return status;
    }
}
