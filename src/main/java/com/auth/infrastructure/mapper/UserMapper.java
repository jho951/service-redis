package com.auth.infrastructure.mapper;

import com.auth.domain.User;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.Optional;
import java.util.UUID;


 @Mapper
 public interface UserMapper {
     @Insert("""
    INSERT INTO users (id, username, password, enabled, created_at, updated_at)
    VALUES (#{id}::uuid, #{username}, #{password}, COALESCE(#{enabled}, TRUE), NOW(), NOW())
    """)
     int insert(User u);

     @Select("""
    SELECT id, username, password, enabled, created_at, updated_at, deleted_at
    FROM users
    WHERE username = #{username}
    LIMIT 1
    """)
     @Results(id="UserMap", value = {
             @Result(property="id", column="id", jdbcType=JdbcType.OTHER, javaType=UUID.class),
             @Result(property="username", column="username"),
             @Result(property="password", column="password"),
             @Result(property="enabled", column="enabled"),
             @Result(property="createdAt", column="created_at"),
             @Result(property="updatedAt", column="updated_at"),
             @Result(property="deletedAt", column="deleted_at"),
     })
     Optional<User> findByUsername(@Param("username") String username);

    @Select("""
    SELECT id, username, password, enabled, created_at, updated_at, deleted_at
    FROM users
    WHERE id = #{id}::uuid
    LIMIT 1
    """)
    @ResultMap("UserMap")
    Optional<User> findById(@Param("id") UUID id);

    @Update("""
    UPDATE users SET deleted_at = NOW()
    WHERE id = #{id}::uuid AND deleted_at IS NULL
    """)
    int softDelete(@Param("id") UUID id);

    @Update("""
    UPDATE users SET deleted_at = NULL
    WHERE id = #{id}::uuid AND deleted_at IS NOT NULL
    """)
    int restore(@Param("id") UUID id);
    }

