package com.drawer.domain;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


/**
 * @author YJH
 * {@value} drawerId 외래 키
 * {@value} vectorJson 벡터 데이터 string 화
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Payload {
	
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    private String vectorJson;
}
