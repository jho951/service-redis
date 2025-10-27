package com.config.mybatis.handler;

import java.sql.ResultSet;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.MappedJdbcTypes;

import com.common.error.ErrorCode;
import com.common.error.AppException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@MappedTypes(JsonNode.class)
@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.LONGVARCHAR, JdbcType.CLOB, JdbcType.OTHER})
public class JsonNodeTypeHandler extends BaseTypeHandler<JsonNode> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, JsonNode parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, parameter.toString());
    }

    @Override
    public JsonNode getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(columnName, rs.getString(columnName));
    }

    @Override
    public JsonNode getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse("index " + columnIndex, rs.getString(columnIndex));
    }

    @Override
    public JsonNode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse("index " + columnIndex, cs.getString(columnIndex));
    }

    private JsonNode parse(String where, String s) throws SQLException {
        if (s == null) return null;
        try {
            return MAPPER.readTree(s);
        } catch (IOException e) {
            throw new AppException(ErrorCode.JSON_PARSE, "Invalid JSON at " + where);
        }
    }
}
