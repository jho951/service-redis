package com.config.mybatis.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.nio.ByteBuffer;
import java.sql.*;
import java.util.UUID;

@MappedTypes(UUIDBinaryTypeHandler.class)
@MappedJdbcTypes(JdbcType.BINARY)
public class UUIDBinaryTypeHandler extends BaseTypeHandler<UUID> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, UUID parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setBytes(i, toBytes(parameter));
  }

  @Override
  public UUID getNullableResult(ResultSet rs, String columnName) throws SQLException {
    byte[] b = rs.getBytes(columnName);
    return (b == null) ? null : fromBytes(b);
  }

  @Override
  public UUID getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    byte[] b = rs.getBytes(columnIndex);
    return (b == null) ? null : fromBytes(b);
  }

  @Override
  public UUID getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    byte[] b = cs.getBytes(columnIndex);
    return (b == null) ? null : fromBytes(b);
  }

  private static byte[] toBytes(UUID uuid) {
    ByteBuffer bb = ByteBuffer.allocate(16);
    bb.putLong(uuid.getMostSignificantBits());
    bb.putLong(uuid.getLeastSignificantBits());
    return bb.array();
  }

  private static UUID fromBytes(byte[] bytes) {
    if (bytes == null || bytes.length != 16)
      throw new IllegalArgumentException("Invalid UUID byte[] length: " + (bytes == null ? 0 : bytes.length));
    ByteBuffer bb = ByteBuffer.wrap(bytes);
    long msb = bb.getLong();
    long lsb = bb.getLong();
    return new UUID(msb, lsb);
  }
}
