package com.drawer.interfaces.api;

import com.common.web.ApiResponse;
import com.drawer.application.DrawerImportService;
import com.drawer.interfaces.dto.request.DrawerImportRequest;
import com.drawer.interfaces.dto.response.IdResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/drawings/import")
@RequiredArgsConstructor
public class DrawerImportController {

    private final DrawerImportService importService;

    /** 외부 vectorJson 임포트 */
    @PostMapping
    public ApiResponse<IdResponse> importVector(@Valid @RequestBody DrawerImportRequest req) {
        var result = importService.saveFromVectorJson(req); // 새 DTO 그대로 전달
        return ApiResponse.ok(IdResponse.of(result.id()));
    }
}
