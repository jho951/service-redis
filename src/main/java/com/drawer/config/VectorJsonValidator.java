package com.drawer.config;

import com.common.error.ErrorCode;
import com.common.error.AppException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.*;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class VectorJsonValidator {

    private final ObjectMapper om = new ObjectMapper();
    private JsonSchema schema;

    @PostConstruct
    public void loadSchema() throws Exception {
        try (InputStream in = new ClassPathResource("schema/vector.schema.json").getInputStream()) {
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
            this.schema = factory.getSchema(in);
        }
    }

    public void validateOrThrow(String json) {
        try {
            JsonNode node = om.readTree(json);
            Set<ValidationMessage> errors = schema.validate(node);
            if (!errors.isEmpty()) {
                String msg = errors.stream().map(ValidationMessage::getMessage)
                        .collect(Collectors.joining("; "));
                throw new AppException(ErrorCode.BAD_REQUEST, msg);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.BAD_REQUEST, "에러 발생");
        }
    }
}
