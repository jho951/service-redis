package com.drawer.config;


import com.networknt.schema.JsonSchema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
public class SchemaConfig {

    @Bean
    public JsonSchema vectorDocSchema() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/schema/vector.schema.json")) {
            if (in == null) throw new IllegalStateException("vector.schema.json not found");
            com.networknt.schema.JsonSchemaFactory factory =
                    com.networknt.schema.JsonSchemaFactory.getInstance(com.networknt.schema.SpecVersion.VersionFlag.V202012);
            ObjectMapper om = new ObjectMapper();
            JsonNode node = om.readTree(in);
            return factory.getSchema(node);
        }
    }
}
