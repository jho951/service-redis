package com.drawer.interfaces.api;

import com.drawer.application.DrawerExportService;
import com.drawer.application.DrawerService;
import com.drawer.interfaces.dto.response.DrawerDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/drawings/export")
@RequiredArgsConstructor
public class DrawerExportController {

    private final DrawerExportService exportService;
    private final DrawerService drawerService;

    /** 단건 XLSX 다운로드: 서비스에서 새 DTO를 이용해 조회 후, 도메인으로 변환/제작 */
    @GetMapping(value = "/{id}.xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportSingle(@PathVariable UUID id) {
        // 상세를 새 DTO로 가져온 뒤, ExportService에서 필요로 하는 도메인/필드를 채워 workbook 생성
        DrawerDetailResponse d = drawerService.getDetail(id);

        byte[] bytes = exportService.makeSingleWorkbookFromDto(d);
        String filename = encodeFilename("drawer-" + id + ".xlsx");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(bytes);
    }

    /** 목록 XLSX 다운로드: 서비스에서 새 DTO 페이지를 사용해 생성 */
    @GetMapping(value = "/list.xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportList(
            @RequestParam(defaultValue = "1000") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "false") boolean deleted
    ) {
        // page/size → limit/offset 변환은 서비스 내부에서 처리하도록 위임하거나 여기서 변환
        int page = (limit > 0) ? (offset / limit) + 1 : 1;
        byte[] bytes = exportService.makeListWorkbookFromDto(drawerService.getList(page, limit, deleted));

        String filename = encodeFilename("drawings-list.xlsx");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(bytes);
    }

    private String encodeFilename(String filename) {
        return URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
    }
}
