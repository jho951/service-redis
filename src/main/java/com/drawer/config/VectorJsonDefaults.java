
package com.drawer.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * @author YJH
 * @description vector JSON 템플릿을 메모리에 올려두는 싱글턴
 * 애플리케이션 시작 시 한 번 읽어 메모리에 캐시해 두고, 필요할 때 기본 벡터 JSON 문자열을 꺼내 쓰는 싱글턴 컴포넌트
 */
@Component
public class VectorJsonDefaults {
    private final String defaultJson;

    public VectorJsonDefaults() throws Exception {
        var res = new ClassPathResource("schema/vector.default.json");
        byte[] bytes = res.getInputStream().readAllBytes();
        this.defaultJson = new String(bytes, StandardCharsets.UTF_8).trim();
    }

    public String asString() { return defaultJson; }
}
