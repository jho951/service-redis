package com.config.mybatis;

import com.config.mybatis.handler.PostgresUUIDTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = {
        "com.drawer.infrastructure.mapper",
        "com.auth.infrastructure.mapper"
})
public class MyBatisConfig {

    @Bean
    public ConfigurationCustomizer postgresUuidHandlerCustomizer() {
        return (org.apache.ibatis.session.Configuration cfg) -> {
            cfg.getTypeHandlerRegistry()
                    .register(java.util.UUID.class, JdbcType.OTHER, new PostgresUUIDTypeHandler());
        };
    }
}
