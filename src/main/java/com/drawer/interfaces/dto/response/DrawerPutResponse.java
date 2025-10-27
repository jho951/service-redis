package com.drawer.interfaces.dto.response;

import com.drawer.domain.Drawer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/** 수정 후 반환 응답 */
@Setter
@Getter
public class DrawerPutResponse {
    private UUID id;
    private String title;
    private Integer version;
    private LocalDateTime updatedAt;

    public DrawerPutResponse() {}

    public DrawerPutResponse(UUID id, String title, Integer version, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.version = version;
        this.updatedAt = updatedAt;
    }

    public static DrawerPutResponse from(Drawer d) {
        return new DrawerPutResponse(d.getId(), d.getTitle(), d.getVersion(), d.getUpdatedAt());
    }

}
