package com.auth.infrastructure.mapper;

import com.auth.domain.Role;
import org.apache.ibatis.annotations.*;

import java.util.Optional;

@Mapper
public interface RoleMapper {
    @Insert("""
        INSERT INTO roles(name) VALUES(#{name})
        ON CONFLICT (name) DO NOTHING
    """)
    int insertIfNotExists(@Param("name") String name);

    @Select("SELECT id, name FROM roles WHERE name = #{name} LIMIT 1")
    Optional<Role> findByName(@Param("name") String name);
}
