package com.drawer.application;

import com.common.error.AppException;
import com.common.error.ErrorCode;
import com.common.web.PageResponse;
import com.drawer.config.VectorJsonDefaults;
import com.drawer.config.VectorJsonValidator;
import com.drawer.domain.Drawer;
import com.drawer.domain.Payload;
import com.drawer.interfaces.dto.request.DrawerCreateRequest;
import com.drawer.interfaces.dto.request.DrawerUpdateRequest;
import com.drawer.interfaces.dto.response.DrawerDetailResponse;
import com.drawer.interfaces.dto.response.DrawerListItemResponse;
import com.drawer.interfaces.dto.response.DrawerPutResponse;
import com.drawer.infrastructure.mapper.DrawerMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DrawerService {

    private final ObjectMapper objectMapper;
    private final DrawerMapper mapper;
    private final VectorJsonDefaults defaults;
    private final VectorJsonValidator validator;

    @Transactional
    public UUID create(DrawerCreateRequest req) {
        final String title = req.title();
        final Object raw = req.vectorJson();
        final UUID id = UUID.randomUUID();

        if (title == null || title.isBlank())
            throw new AppException(ErrorCode.BAD_REQUEST, "제목은 필수입니다.");

        if (mapper.existsTitle(title))
            throw new AppException(ErrorCode.DUPLICATED_TITLE, "같은 이름이 존재합니다. " + title);

        final String vectorJsonStr;
        try {
            if (raw == null) {
                vectorJsonStr = defaults.asString();
            } else if (raw instanceof String s) {
                objectMapper.readTree(s); // JSON 유효성만 확인
                vectorJsonStr = s;
            } else {
                vectorJsonStr = objectMapper.writeValueAsString(raw);
            }
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.BAD_REQUEST, "vectorJson이 올바른 JSON이 아닙니다.");
        }

        validator.validateOrThrow(vectorJsonStr);

        Drawer d = new Drawer();
        d.setId(id);
        d.setTitle(title);
        d.setVersion(0);
        if (mapper.insertMeta(d) != 1)
            throw new AppException(ErrorCode.BAD_REQUEST, "메타 저장에 실패했습니다.");

        Payload p = new Payload();
        p.setId(id);
        p.setVectorJson(vectorJsonStr);
        if (mapper.insertPayload(p) != 1)
            throw new AppException(ErrorCode.BAD_REQUEST, "페이로드 저장에 실패했습니다.");

        return id;
    }

    @Transactional(readOnly = true)
    public DrawerDetailResponse getDetail(UUID id) {
        Drawer d = mapper.findByIdWithPayload(id)
                .orElseThrow(() -> new AppException(ErrorCode.DRAWING_NOT_FOUND, "존재하지 않거나 삭제된 Drawer 입니다. " + id));
        String vectorJson = d.getPayload() != null ? d.getPayload().getVectorJson() : null;

        return new DrawerDetailResponse(
                d.getId(), d.getTitle(),
                d.getCreatedAt(), d.getUpdatedAt(), d.getDeletedAt(),
                d.getVersion(), vectorJson
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<DrawerListItemResponse> getList(int page, int size, boolean deleted) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, Math.min(size, 200));
        int offset = (safePage - 1) * safeSize;

        List<Drawer> items = deleted ? mapper.findDeletedPage(safeSize, offset)
                : mapper.findPage(safeSize, offset);
        long total = deleted ? mapper.deletedCount() : mapper.count();

        var rows = items.stream().map(DrawerListItemResponse::from).toList();
        return PageResponse.of(rows, safePage, safeSize, total);
    }

    @Transactional
    public DrawerPutResponse update(UUID id, DrawerUpdateRequest req) {
        final String title = req.title();
        final Object raw = req.vectorJson();

        if (mapper.existsTitleOther(id, title))
            throw new AppException(ErrorCode.DUPLICATED_TITLE, "같은 이름이 존재합니다. " + title);

        final String vectorJsonStr;
        try {
            if (raw == null) {
                vectorJsonStr = defaults.asString();
            } else if (raw instanceof String s) {
                objectMapper.readTree(s);
                vectorJsonStr = s;
            } else {
                vectorJsonStr = objectMapper.writeValueAsString(raw);
            }
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.BAD_REQUEST, "vectorJson이 올바른 JSON이 아닙니다.");
        }
        validator.validateOrThrow(vectorJsonStr);

        int updatedTitle = mapper.updateDrawerTitle(id, title);
        if (updatedTitle == 0)
            throw new AppException(ErrorCode.DRAWING_NOT_FOUND, "버전 불일치 또는 존재하지 않음 " + id);

        int updatedPayload = mapper.updatePayload(id, vectorJsonStr);
        if (updatedPayload == 0) {
            mapper.insertPayload(new Payload(id, vectorJsonStr));
        }

        Drawer d = mapper.findByIdWithPayload(id)
                .orElseThrow(() -> new AppException(ErrorCode.DRAWING_NOT_FOUND, "존재하지 않거나 삭제된 Drawer 입니다. " + id));

        return DrawerPutResponse.from(d);
    }

    @Transactional
    public void deleteSoft(UUID id) {
        if (mapper.deleteSoft(id) == 0)
            throw new AppException(ErrorCode.DRAWING_NOT_FOUND, "존재하지 않거나 삭제된 Drawer 입니다. " + id);
    }

    @Transactional
    public void restore(UUID id) {
        if (mapper.restore(id) == 0)
            throw new AppException(ErrorCode.DRAWING_NOT_DELETED, "삭제 상태가 아니거나 존재하지 않습니다. " + id);
    }

    @Transactional
    public void deleteHard(UUID id) {
        mapper.deletePayloadByDrawerId(id);
        if (mapper.deleteHard(id) == 0)
            throw new AppException(ErrorCode.DRAWING_NOT_FOUND, "존재하지 않거나 이미 삭제된 Drawer 입니다. " + id);
    }
}
