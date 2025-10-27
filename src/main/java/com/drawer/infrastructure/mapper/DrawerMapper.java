package com.drawer.infrastructure.mapper;

import com.drawer.domain.Drawer;
import com.drawer.domain.Payload;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface DrawerMapper {

    /* ========= exists ========= */

    @Select("""
        SELECT EXISTS(
          SELECT 1 FROM drawer
          WHERE deleted_at IS NULL
            AND title = #{title}
        )
    """)
    boolean existsTitle(@Param("title") String title);

    @Select("""
        SELECT EXISTS(
          SELECT 1 FROM drawer
          WHERE deleted_at IS NULL
            AND title = #{title}
            AND id <> #{id, javaType=java.util.UUID, jdbcType=OTHER}
        )
    """)
    boolean existsTitleOther(@Param("id") UUID id, @Param("title") String title);


    /* ========= insert ========= */

    @Insert("""
        INSERT INTO drawer (id, title, version, created_at, updated_at)
        VALUES (#{id, javaType=java.util.UUID, jdbcType=OTHER}, #{title}, #{version}, NOW(), NOW())
    """)
    int insertMeta(Drawer d);

    @Insert("""
        INSERT INTO drawer_payload (drawer_id, vector_json)
        VALUES (#{id, javaType=java.util.UUID, jdbcType=OTHER}, CAST(#{vectorJson} AS jsonb))
    """)
    int insertPayload(Payload p);


    /* ========= select (join) ========= */

    @Select("""
        SELECT
          d.id            AS d_id,
          d.title         AS d_title,
          d.created_at    AS d_created_at,
          d.updated_at    AS d_updated_at,
          d.deleted_at    AS d_deleted_at,
          d.version       AS d_version,
          p.drawer_id     AS p_drawer_id,
          (p.vector_json)::text AS p_vector_json
        FROM drawer d
        LEFT JOIN drawer_payload p ON p.drawer_id = d.id
        WHERE d.id = #{id, javaType=java.util.UUID, jdbcType=OTHER}
        LIMIT 1
    """)
    @Results(id = "DrawerWithPayloadMap", value = {
            @Result(property = "id",         column = "d_id"),
            @Result(property = "title",      column = "d_title"),
            @Result(property = "createdAt",  column = "d_created_at"),
            @Result(property = "updatedAt",  column = "d_updated_at"),
            @Result(property = "deletedAt",  column = "d_deleted_at"),
            @Result(property = "version",    column = "d_version"),
            @Result(property = "payload.id",         column = "p_drawer_id"),
            @Result(property = "payload.vectorJson", column = "p_vector_json")
    })
    Optional<Drawer> findByIdWithPayload(@Param("id") UUID id);


    /* ========= payload 단건 조회 ========= */

    @Select("""
        SELECT drawer_id AS id,
               (vector_json)::text AS vector_json
        FROM drawer_payload
        WHERE drawer_id = #{id, javaType=java.util.UUID, jdbcType=OTHER}
        LIMIT 1
    """)
    @Results(id = "PayloadMap", value = {
            @Result(property = "id",         column = "id"),
            @Result(property = "vectorJson", column = "vector_json")
    })
    Optional<Payload> findPayloadById(@Param("id") UUID id);


    /* ========= paging ========= */

    @Select("""
        SELECT id, title, created_at, updated_at, deleted_at, version
        FROM drawer
        WHERE deleted_at IS NULL
        ORDER BY updated_at DESC
        LIMIT #{limit} OFFSET #{offset}
    """)
    @Results(id = "DrawerMap", value = {
            @Result(property = "id",         column = "id"),
            @Result(property = "title",      column = "title"),
            @Result(property = "createdAt",  column = "created_at"),
            @Result(property = "updatedAt",  column = "updated_at"),
            @Result(property = "deletedAt",  column = "deleted_at"),
            @Result(property = "version",    column = "version")
    })
    List<Drawer> findPage(@Param("limit") int limit, @Param("offset") int offset);

    @Select("""
        SELECT id, title, created_at, updated_at, deleted_at, version
        FROM drawer
        WHERE deleted_at IS NOT NULL
        ORDER BY updated_at DESC
        LIMIT #{limit} OFFSET #{offset}
    """)
    @ResultMap("DrawerMap")
    List<Drawer> findDeletedPage(@Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM drawer WHERE deleted_at IS NULL")
    long count();

    @Select("SELECT COUNT(*) FROM drawer WHERE deleted_at IS NOT NULL")
    long deletedCount();


    /* ========= update ========= */

    @Update("""
        UPDATE drawer
        SET title = #{title},
            version = version + 1,
            updated_at = NOW()
        WHERE id = #{id, javaType=java.util.UUID, jdbcType=OTHER}
          AND deleted_at IS NULL
    """)
    int updateDrawerTitle(@Param("id") UUID id, @Param("title") String title);

    @Update("""
        UPDATE drawer
        SET title = #{title},
            version = version + 1,
            updated_at = NOW()
        WHERE id = #{id, javaType=java.util.UUID, jdbcType=OTHER}
          AND version = #{version}
          AND deleted_at IS NULL
    """)
    int updateDrawerTitleWithVersion(@Param("id") UUID id,
                                     @Param("title") String title,
                                     @Param("version") int version);

    @Update("""
        UPDATE drawer_payload
        SET vector_json = CAST(#{vectorJson} AS jsonb)
        WHERE drawer_id = #{id, javaType=java.util.UUID, jdbcType=OTHER}
    """)
    int updatePayload(@Param("id") UUID id, @Param("vectorJson") String vectorJson);


    /* ========= soft delete / restore ========= */

    @Update("""
        UPDATE drawer
        SET deleted_at = NOW(),
            updated_at = NOW()
        WHERE id = #{id, javaType=java.util.UUID, jdbcType=OTHER}
          AND deleted_at IS NULL
    """)
    int deleteSoft(@Param("id") UUID id);

    @Update("""
        UPDATE drawer
        SET deleted_at = NULL,
            updated_at = NOW()
        WHERE id = #{id, javaType=java.util.UUID, jdbcType=OTHER}
          AND deleted_at IS NOT NULL
    """)
    int restore(@Param("id") UUID id);


    /* ========= delete ========= */

    @Delete("DELETE FROM drawer_payload WHERE drawer_id = #{id, javaType=java.util.UUID, jdbcType=OTHER}")
    int deletePayloadByDrawerId(@Param("id") UUID id);

    @Delete("DELETE FROM drawer WHERE id = #{id, javaType=java.util.UUID, jdbcType=OTHER}")
    int deleteHard(@Param("id") UUID id);
}
