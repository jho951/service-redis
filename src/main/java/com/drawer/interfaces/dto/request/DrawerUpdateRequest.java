package com.drawer.interfaces.dto.request;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;


@Schema(name = "DrawerUpdateRequest", description = "그림 수정 요청")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DrawerUpdateRequest(
        @Schema(description = "도면 제목", example = "Landing Page Sketch v2", maxLength = 255)
        @NotBlank
        String title,

        @Schema(
                description = "벡터 JSON(자유 구조). null이면 서버에서 기존 값 유지",
                implementation = Object.class,
                nullable = true
        )
        JsonNode vectorJson,

        @Schema(
                description = "낙관적 락용 버전(선택). 서버가 version 기반 업데이트를 할 때 사용",
                example = "2",
                nullable = true
        )
        Integer version
) {}