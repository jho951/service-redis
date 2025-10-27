package com.auth.domain;

import com.common.domain.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class User extends BaseEntity {
    private UUID id;
    private String username;
    private String password;
    private Boolean enabled;
}
