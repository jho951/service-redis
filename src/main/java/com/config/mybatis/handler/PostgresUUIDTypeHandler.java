package com.config.mybatis.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.nio.ByteBuffer;
import java.sql.*;
import java.util.UUID;

/**
 * PostgreSQL uuid 컬럼과 java.util.UUID 간 매핑을 담당.
 * - setParameter: ps.setObject(..., Types.OTHER) 로 uuid 바인딩
 * - getResult: uuid / String / bytea(16바이트) 모두 대응
 */
@MappedTypes(UUID.class)
@MappedJdbcTypes(JdbcType.OTHER) // Postgres의 uuid는 JDBC에서 OTHER 로 다뤄짐
public class PostgresUUIDTypeHandler extends BaseTypeHandler<UUID> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UUID parameter, JdbcType jdbcType)
            throws SQLException {
        // Postgres 드라이버는 uuid를 Types.OTHER로 setObject 하는 게 표준적
        ps.setObject(i, parameter, Types.OTHER);
    }

    @Override
    public UUID getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return readUUID(rs.getObject(columnName), () -> rs.getBytes(columnName));
    }

    @Override
    public UUID getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return readUUID(rs.getObject(columnIndex), () -> rs.getBytes(columnIndex));
    }

    @Override
    public UUID getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return readUUID(cs.getObject(columnIndex), () -> cs.getBytes(columnIndex));
    }

    /* ---------- 내부 헬퍼 ---------- */

    private interface BytesSupplier { byte[] get() throws SQLException; }

    private UUID readUUID(Object obj, BytesSupplier bytesSupplier) throws SQLException {
        if (obj == null) return null;

        // 1) 드라이버가 이미 UUID로 반환하는 경우
        if (obj instanceof UUID uuid) return uuid;

        // 2) 문자열로 들어오는 경우 ('uuid'를 text로 캐스팅한 select 등)
        if (obj instanceof String s) {
            String trimmed = s.trim();
            if (trimmed.isEmpty()) return null;
            return UUID.fromString(trimmed);
        }

        // 3) bytea(16바이트)로 들어오는 경우 방어적 처리
        if (obj instanceof byte[] bytes) {
            if (bytes.length == 16) return fromBytes(bytes);
        } else {
            // 일부 드라이버/버전에서 getObject가 기타 타입으로 올 수 있어 bytes 재시도
            byte[] maybeBytes = bytesSupplier.get();
            if (maybeBytes != null && maybeBytes.length == 16) return fromBytes(maybeBytes);
        }

        // 마지막 방어: 드라이버가 UUID.class로 직접 매핑 가능한 경우 (JDBC 4.2+)
        try {
            if (obj instanceof java.sql.ResultSet) {
                // 이 분기는 보통 오지 않지만, 안전 빌드 유지
            }
        } catch (Throwable ignore) {}

        // 타입을 더 이상 식별할 수 없으면 문자열 변환 시도
        return UUID.fromString(obj.toString());
    }

    private UUID fromBytes(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long high = bb.getLong();
        long low  = bb.getLong();
        return new UUID(high, low);
    }
}
