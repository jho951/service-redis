package com.drawer.interfaces.dto.response;

import com.drawer.domain.Drawer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
public class DrawerDetailResponse {
    private UUID id;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Integer version;
    private String vectorJson; // payload 포함

    public DrawerDetailResponse() {}

    public DrawerDetailResponse(UUID id, String title,
                                LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt,
                                Integer version, String vectorJson) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.version = version;
        this.vectorJson = vectorJson;
    }

    public static DrawerDetailResponse from(Drawer d) {
        String vj = d.getPayload() != null ? d.getPayload().getVectorJson() : null;
        return new DrawerDetailResponse(
                d.getId(), d.getTitle(),
                d.getCreatedAt(), d.getUpdatedAt(), d.getDeletedAt(),
                d.getVersion(), vj
        );
    }

}
