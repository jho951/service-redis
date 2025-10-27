package com.drawer.application;

import com.common.error.AppException;
import com.common.error.ErrorCode;
import com.drawer.domain.Drawer;
import com.drawer.domain.Payload;
import com.drawer.interfaces.dto.request.DrawerImportRequest;
import com.drawer.infrastructure.mapper.DrawerMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DrawerImportService {

    private final DrawerMapper mapper;
    private final ObjectMapper om;
    private final JsonSchema vectorDocSchema;

    /** 컨트롤러에 의존하지 않는 반환 DTO */
    public record Imported(UUID id, String title, Integer version) {}

    /**
     * 외부 vectorJson(새 DTO)을 검증 후 저장
     */
    @Transactional
    public Imported saveFromVectorJson(DrawerImportRequest req) {
        String vectorJson = Optional.ofNullable(req.vectorJson())
                .filter(s -> !s.isBlank())
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "vectorJson이 비었습니다."));

        final JsonNode node;
        try {
            node = om.readTree(vectorJson);
        } catch (Exception e) {
            throw new AppException(ErrorCode.BAD_REQUEST, "vectorJson 파싱 실패");
        }

        Set<ValidationMessage> errors = vectorDocSchema.validate(node);
        if (!errors.isEmpty()) {
            String msg = errors.stream().limit(3).map(ValidationMessage::getMessage).collect(Collectors.joining("; "));
            throw new AppException(ErrorCode.BAD_REQUEST, "스키마 검증 실패: " + msg);
        }

        String title = Optional.ofNullable(req.titleHint()).orElseGet(() ->
                Optional.ofNullable(node.path("meta").path("title").asText(null))
                        .orElse("Imported " + LocalDateTime.now())
        );

        UUID id = UUID.randomUUID();
        int version = 0;

        Drawer d = new Drawer();
        d.setId(id);
        d.setTitle(title);
        d.setVersion(version);
        mapper.insertMeta(d);

        Payload p = new Payload();
        p.setId(id);
        p.setVectorJson(vectorJson);
        mapper.insertPayload(p);

        return new Imported(id, title, version);
    }
}
