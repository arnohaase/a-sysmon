package com.ajjpj.asysmon.measure.jdbc;


import com.ajjpj.asysmon.ASysMon;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

/**
 * @author arno
 */
public class ASysMonCallableStatement extends ASysMonPreparedStatement implements CallableStatement {
    private final CallableStatement inner;

    public ASysMonCallableStatement(Connection conn, CallableStatement inner, ASysMon sysMon, String sql) {
        super(conn, inner, sysMon, sql);
        this.inner = inner;
    }


    //--------------------- setting parameters

    private void setSysMonParam(String parameterName, Object value) {
        m.addParameter(batchCount == 0 ? parameterName : "#" + batchCount + ": " + parameterName, String.valueOf(value));
    }

    @Override public void setURL(String parameterName, URL val) throws SQLException {
        setSysMonParam(parameterName, val);
        inner.setURL(parameterName, val);
    }

    @Override public void setNull(String parameterName, int sqlType) throws SQLException {
        setSysMonParam(parameterName, null);
        inner.setNull(parameterName, sqlType);
    }

    @Override public void setBoolean(String parameterName, boolean x) throws SQLException {
        setSysMonParam(parameterName, x);
        inner.setBoolean(parameterName, x);
    }

    @Override public void setByte(String parameterName, byte x) throws SQLException {
        setSysMonParam(parameterName, x);
        inner.setByte(parameterName, x);
    }

    @Override public void setShort(String parameterName, short x) throws SQLException {
        setSysMonParam(parameterName, x);
        inner.setShort(parameterName, x);
    }

    @Override public void setInt(String parameterName, int x) throws SQLException {
        setSysMonParam(parameterName, x);
        inner.setInt(parameterName, x);
    }

    @Override public void setLong(String parameterName, long x) throws SQLException {
        setSysMonParam(parameterName, x);
        inner.setLong(parameterName, x);
    }

    @Override public void setFloat(String parameterName, float x) throws SQLException {
        setSysMonParam(parameterName, x);
        inner.setFloat(parameterName, x);
    }

    @Override public void setDouble(String parameterName, double x) throws SQLException {
        setSysMonParam(parameterName, x);
        inner.setDouble(parameterName, x);
    }

    @Override public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
        setSysMonParam(parameterName, x);
        inner.setBigDecimal(parameterName, x);
    }

    @Override public void setString(String parameterName, String x) throws SQLException {
        setSysMonParam(parameterName, x);
        inner.setString(parameterName, x);
    }

    @Override public void setBytes(String parameterName, byte[] x) throws SQLException {
        setSysMonParam(parameterName, x);
        inner.setBytes(parameterName, x);
    }

    @Override public void setDate(String parameterName, Date x) throws SQLException {
        setSysMonParam(parameterName, x);
        inner.setDate(parameterName, x);
    }

    @Override public void setTime(String parameterName, Time x) throws SQLException {
        setSysMonParam(parameterName, x);
        inner.setTime(parameterName, x);
    }

    @Override public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
        setSysMonParam(parameterName, x);
        inner.setTimestamp(parameterName, x);
    }

    @Override public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
        setSysMonParam(parameterName, x);
        inner.setAsciiStream(parameterName, x, length);
    }

    @Override public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {
        setSysMonParam(parameterName, x);
        inner.setBinaryStream(parameterName, x, length);
    }

    @Override public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
        setSysMonParam(parameterName, x);
        inner.setObject(parameterName, x, targetSqlType, scale);
    }

    @Override public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
        setSysMonParam(parameterName, x);
        inner.setObject(parameterName, x, targetSqlType);
    }

    @Override public void setObject(String parameterName, Object x) throws SQLException {
        setSysMonParam(parameterName, x);
        inner.setObject(parameterName, x);
    }

    @Override public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
        setSysMonParam(parameterName, reader);
        inner.setCharacterStream(parameterName, reader, length);
    }

    @Override public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
        setSysMonParam(parameterName, x);
        inner.setDate(parameterName, x, cal);
    }

    @Override public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
        setSysMonParam(parameterName, x);
        inner.setTime(parameterName, x, cal);
    }

    @Override public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
        setSysMonParam(parameterName, x);
        inner.setTimestamp(parameterName, x, cal);
    }

    @Override public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
        setSysMonParam(parameterName, null);
        inner.setNull(parameterName, sqlType, typeName);
    }

    @Override public void setRowId(String parameterName, RowId x) throws SQLException {
        setSysMonParam(parameterName, null);
        inner.setRowId(parameterName, x);
    }

    @Override public void setNString(String parameterName, String value) throws SQLException {
        setSysMonParam(parameterName, null);
        inner.setNString(parameterName, value);
    }

    @Override public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {
        setSysMonParam(parameterName, null);
        inner.setNCharacterStream(parameterName, value, length);
    }

    @Override public void setNClob(String parameterName, NClob value) throws SQLException {
        setSysMonParam(parameterName, null);
        inner.setNClob(parameterName, value);
    }

    @Override public void setClob(String parameterName, Reader reader, long length) throws SQLException {
        setSysMonParam(parameterName, null);
        inner.setClob(parameterName, reader, length);
    }

    @Override public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {
        setSysMonParam(parameterName, null);
        inner.setBlob(parameterName, inputStream, length);
    }

    @Override public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
        setSysMonParam(parameterName, null);
        inner.setNClob(parameterName, reader, length);
    }

    @Override public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
        setSysMonParam(parameterName, null);
        inner.setSQLXML(parameterName, xmlObject);
    }

    @Override public void setBlob(String parameterName, Blob x) throws SQLException {
        setSysMonParam(parameterName, null);
        inner.setBlob(parameterName, x);
    }

    @Override public void setClob(String parameterName, Clob x) throws SQLException {
        setSysMonParam(parameterName, null);
        inner.setClob(parameterName, x);
    }

    @Override public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException {
        setSysMonParam(parameterName, null);
        inner.setAsciiStream(parameterName, x, length);
    }

    @Override public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {
        setSysMonParam(parameterName, null);
        inner.setBinaryStream(parameterName, x, length);
    }

    @Override public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
        setSysMonParam(parameterName, null);
        inner.setCharacterStream(parameterName, reader, length);
    }

    @Override public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
        setSysMonParam(parameterName, null);
        inner.setAsciiStream(parameterName, x);
    }

    @Override public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
        setSysMonParam(parameterName, null);
        inner.setBinaryStream(parameterName, x);
    }

    @Override public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
        setSysMonParam(parameterName, null);
        inner.setCharacterStream(parameterName, reader);
    }

    @Override public void setNCharacterStream(String parameterName, Reader value) throws SQLException {
        setSysMonParam(parameterName, null);
        inner.setNCharacterStream(parameterName, value);
    }

    @Override public void setClob(String parameterName, Reader reader) throws SQLException {
        setSysMonParam(parameterName, null);
        inner.setClob(parameterName, reader);
    }

    @Override public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
        setSysMonParam(parameterName, null);
        inner.setBlob(parameterName, inputStream);
    }

    @Override public void setNClob(String parameterName, Reader reader) throws SQLException {
        setSysMonParam(parameterName, null);
        inner.setNClob(parameterName, reader);
    }

    //--------------------- ignored by ASysMon

    @Override
    public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
        inner.registerOutParameter(parameterIndex, sqlType);
    }

    @Override
    public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {
        inner.registerOutParameter(parameterIndex, sqlType, scale);
    }

    @Override
    public boolean wasNull() throws SQLException {
        return inner.wasNull();
    }

    @Override
    public String getString(int parameterIndex) throws SQLException {
        return inner.getString(parameterIndex);
    }

    @Override
    public boolean getBoolean(int parameterIndex) throws SQLException {
        return inner.getBoolean(parameterIndex);
    }

    @Override
    public byte getByte(int parameterIndex) throws SQLException {
        return inner.getByte(parameterIndex);
    }

    @Override
    public short getShort(int parameterIndex) throws SQLException {
        return inner.getShort(parameterIndex);
    }

    @Override
    public int getInt(int parameterIndex) throws SQLException {
        return inner.getInt(parameterIndex);
    }

    @Override
    public long getLong(int parameterIndex) throws SQLException {
        return inner.getLong(parameterIndex);
    }

    @Override
    public float getFloat(int parameterIndex) throws SQLException {
        return inner.getFloat(parameterIndex);
    }

    @Override
    public double getDouble(int parameterIndex) throws SQLException {
        return inner.getDouble(parameterIndex);
    }

    @Override
    public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
        return inner.getBigDecimal(parameterIndex, scale);
    }

    @Override
    public byte[] getBytes(int parameterIndex) throws SQLException {
        return inner.getBytes(parameterIndex);
    }

    @Override
    public Date getDate(int parameterIndex) throws SQLException {
        return inner.getDate(parameterIndex);
    }

    @Override
    public Time getTime(int parameterIndex) throws SQLException {
        return inner.getTime(parameterIndex);
    }

    @Override
    public Timestamp getTimestamp(int parameterIndex) throws SQLException {
        return inner.getTimestamp(parameterIndex);
    }

    @Override
    public Object getObject(int parameterIndex) throws SQLException {
        return inner.getObject(parameterIndex);
    }

    @Override
    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        return inner.getBigDecimal(parameterIndex);
    }

    @Override
    public Object getObject(int parameterIndex, Map<String, Class<?>> map) throws SQLException {
        return inner.getObject(parameterIndex, map);
    }

    @Override
    public Ref getRef(int parameterIndex) throws SQLException {
        return inner.getRef(parameterIndex);
    }

    @Override
    public Blob getBlob(int parameterIndex) throws SQLException {
        return inner.getBlob(parameterIndex);
    }

    @Override
    public Clob getClob(int parameterIndex) throws SQLException {
        return inner.getClob(parameterIndex);
    }

    @Override
    public Array getArray(int parameterIndex) throws SQLException {
        return inner.getArray(parameterIndex);
    }

    @Override
    public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
        return inner.getDate(parameterIndex, cal);
    }

    @Override
    public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
        return inner.getTime(parameterIndex, cal);
    }

    @Override
    public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
        return inner.getTimestamp(parameterIndex, cal);
    }

    @Override
    public void registerOutParameter(int parameterIndex, int sqlType, String typeName) throws SQLException {
        inner.registerOutParameter(parameterIndex, sqlType, typeName);
    }

    @Override
    public void registerOutParameter(String parameterName, int sqlType) throws SQLException {
        inner.registerOutParameter(parameterName, sqlType);
    }

    @Override
    public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
        inner.registerOutParameter(parameterName, sqlType, scale);
    }

    @Override
    public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {
        inner.registerOutParameter(parameterName, sqlType, typeName);
    }

    @Override
    public URL getURL(int parameterIndex) throws SQLException {
        return inner.getURL(parameterIndex);
    }

    @Override
    public String getString(String parameterName) throws SQLException {
        return inner.getString(parameterName);
    }

    @Override
    public boolean getBoolean(String parameterName) throws SQLException {
        return inner.getBoolean(parameterName);
    }

    @Override
    public byte getByte(String parameterName) throws SQLException {
        return inner.getByte(parameterName);
    }

    @Override
    public short getShort(String parameterName) throws SQLException {
        return inner.getShort(parameterName);
    }

    @Override
    public int getInt(String parameterName) throws SQLException {
        return inner.getInt(parameterName);
    }

    @Override
    public long getLong(String parameterName) throws SQLException {
        return inner.getLong(parameterName);
    }

    @Override
    public float getFloat(String parameterName) throws SQLException {
        return inner.getFloat(parameterName);
    }

    @Override
    public double getDouble(String parameterName) throws SQLException {
        return inner.getDouble(parameterName);
    }

    @Override
    public byte[] getBytes(String parameterName) throws SQLException {
        return inner.getBytes(parameterName);
    }

    @Override
    public Date getDate(String parameterName) throws SQLException {
        return inner.getDate(parameterName);
    }

    @Override
    public Time getTime(String parameterName) throws SQLException {
        return inner.getTime(parameterName);
    }

    @Override
    public Timestamp getTimestamp(String parameterName) throws SQLException {
        return inner.getTimestamp(parameterName);
    }

    @Override
    public Object getObject(String parameterName) throws SQLException {
        return inner.getObject(parameterName);
    }

    @Override
    public BigDecimal getBigDecimal(String parameterName) throws SQLException {
        return inner.getBigDecimal(parameterName);
    }

    @Override
    public Object getObject(String parameterName, Map<String, Class<?>> map) throws SQLException {
        return inner.getObject(parameterName, map);
    }

    @Override
    public Ref getRef(String parameterName) throws SQLException {
        return inner.getRef(parameterName);
    }

    @Override
    public Blob getBlob(String parameterName) throws SQLException {
        return inner.getBlob(parameterName);
    }

    @Override
    public Clob getClob(String parameterName) throws SQLException {
        return inner.getClob(parameterName);
    }

    @Override
    public Array getArray(String parameterName) throws SQLException {
        return inner.getArray(parameterName);
    }

    @Override
    public Date getDate(String parameterName, Calendar cal) throws SQLException {
        return inner.getDate(parameterName, cal);
    }

    @Override
    public Time getTime(String parameterName, Calendar cal) throws SQLException {
        return inner.getTime(parameterName, cal);
    }

    @Override
    public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException {
        return inner.getTimestamp(parameterName, cal);
    }

    @Override
    public URL getURL(String parameterName) throws SQLException {
        return inner.getURL(parameterName);
    }

    @Override
    public RowId getRowId(int parameterIndex) throws SQLException {
        return inner.getRowId(parameterIndex);
    }

    @Override
    public RowId getRowId(String parameterName) throws SQLException {
        return inner.getRowId(parameterName);
    }

    @Override
    public NClob getNClob(int parameterIndex) throws SQLException {
        return inner.getNClob(parameterIndex);
    }

    @Override
    public NClob getNClob(String parameterName) throws SQLException {
        return inner.getNClob(parameterName);
    }

    @Override
    public SQLXML getSQLXML(int parameterIndex) throws SQLException {
        return inner.getSQLXML(parameterIndex);
    }

    @Override
    public SQLXML getSQLXML(String parameterName) throws SQLException {
        return inner.getSQLXML(parameterName);
    }

    @Override
    public String getNString(int parameterIndex) throws SQLException {
        return inner.getNString(parameterIndex);
    }

    @Override
    public String getNString(String parameterName) throws SQLException {
        return inner.getNString(parameterName);
    }

    @Override
    public Reader getNCharacterStream(int parameterIndex) throws SQLException {
        return inner.getNCharacterStream(parameterIndex);
    }

    @Override
    public Reader getNCharacterStream(String parameterName) throws SQLException {
        return inner.getNCharacterStream(parameterName);
    }

    @Override
    public Reader getCharacterStream(int parameterIndex) throws SQLException {
        return inner.getCharacterStream(parameterIndex);
    }

    @Override
    public Reader getCharacterStream(String parameterName) throws SQLException {
        return inner.getCharacterStream(parameterName);
    }
}
