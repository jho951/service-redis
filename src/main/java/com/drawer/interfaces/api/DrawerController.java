package com.drawer.interfaces.api;

import java.util.UUID;

import com.drawer.interfaces.dto.request.DrawerDeleteRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import com.common.web.ApiResponse;
import com.common.web.PageResponse;

import com.drawer.application.DrawerService;
import com.drawer.interfaces.dto.response.IdResponse;
import com.drawer.interfaces.dto.response.DrawerPutResponse;
import com.drawer.interfaces.dto.request.DrawerUpdateRequest;
import com.drawer.interfaces.dto.request.DrawerCreateRequest;
import com.drawer.interfaces.dto.request.DrawerRestoreRequest;
import com.drawer.interfaces.dto.response.DrawerDetailResponse;
import com.drawer.interfaces.dto.response.DrawerListItemResponse;

@RestController
@RequestMapping("/api/v1/drawings")
@RequiredArgsConstructor
public class DrawerController {

    private final DrawerService drawerService;

    /** 생성 */
    @PostMapping
    public ApiResponse<IdResponse> create(@Valid @RequestBody DrawerCreateRequest req) {
        UUID id = drawerService.create(req);
        return ApiResponse.ok(IdResponse.of(id));
    }

    /** 상세 조회 */
    @GetMapping("/{id}")
    public ApiResponse<DrawerDetailResponse> detail(@PathVariable UUID id) {
        DrawerDetailResponse body = drawerService.getDetail(id);
        return ApiResponse.ok(body);
    }

    /** 목록 조회 (page/size/deleted) */
    @GetMapping
    public ApiResponse<PageResponse<DrawerListItemResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean deleted
    ) {
        PageResponse<DrawerListItemResponse> body = drawerService.getList(page, size, deleted);
        return ApiResponse.ok(body);
    }

    /** 수정 */
    @PutMapping("/{id}")
    public ApiResponse<DrawerPutResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody DrawerUpdateRequest req
    ) {
        DrawerPutResponse body = drawerService.update(id, req); // 새 DTO 반환
        return ApiResponse.ok(body);
    }

    /** 소프트 삭제 */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSoft(@PathVariable UUID id, @RequestBody(required = false) DrawerDeleteRequest ignored) {
        drawerService.deleteSoft(id);
        return ApiResponse.ok(null);
    }

    /** 복구 */
    @PostMapping("/{id}/restore")
    public ApiResponse<Void> restore(@PathVariable UUID id, @RequestBody(required = false) DrawerRestoreRequest ignored) {
        drawerService.restore(id);
        return ApiResponse.ok(null);
    }

    /** 하드 삭제 */
    @DeleteMapping("/{id}/hard")
    public ApiResponse<Void> deleteHard(@PathVariable UUID id) {
        drawerService.deleteHard(id);
        return ApiResponse.ok(null);
    }
}
