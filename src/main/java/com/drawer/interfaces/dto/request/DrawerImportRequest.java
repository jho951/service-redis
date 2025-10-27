package com.drawer.interfaces.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DrawerImportRequest(
        @NotBlank String vectorJson,
        String titleHint
) {}
