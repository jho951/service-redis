package com.drawer.domain;

import java.util.UUID;

import com.common.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author YJH
 * {@value} id 기본키
 * {@value} title 제목
 * {@value} payload 벡터데이터가 저장되어 있는 테이블
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Drawer extends BaseEntity {

    @ToString.Include
    @EqualsAndHashCode.Include
    private UUID id;

    @ToString.Include
    private String title;

    private Payload payload;
}
