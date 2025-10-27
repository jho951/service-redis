package com.drawer.interfaces.dto.response;

import com.drawer.domain.Drawer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
public class DrawerListItemResponse {
    private UUID id;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Integer version;

    public DrawerListItemResponse() {}

    public DrawerListItemResponse(UUID id, String title, LocalDateTime createdAt,
                                  LocalDateTime updatedAt, Integer version, LocalDateTime deletedAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
        this.deletedAt = deletedAt;
    }

    public static DrawerListItemResponse from(Drawer d) {
        return new DrawerListItemResponse(
                d.getId(),
                d.getTitle(),
                d.getCreatedAt(),
                d.getUpdatedAt(),
                d.getVersion(),
                d.getDeletedAt()
        );
    }

}
