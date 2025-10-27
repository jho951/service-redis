package com.drawer.interfaces.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;

@Schema(name = "DrawerCreateRequest", description = "그림 생성 요청 DTO")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DrawerCreateRequest(
        @NotBlank
        @Schema(description = "도면 제목", example = "Landing Page Sketch v1", maxLength = 255)
        String title,

        @NotNull
        @Schema(description = "벡터 문서(JSON)", type = "object",
                example = "{\"schema\":1,\"canvas\":{\"width\":800,\"height\":600},\"layers\":[],\"render\":{},\"hitmap\":{}}")
        JsonNode vectorJson
) {}
