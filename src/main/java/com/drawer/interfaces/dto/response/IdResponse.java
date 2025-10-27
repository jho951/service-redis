package com.drawer.interfaces.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class IdResponse {
    private UUID id;

    public IdResponse() {}
    public IdResponse(UUID id) { this.id = id; }

    public static IdResponse of(UUID id) { return new IdResponse(id); }

}
