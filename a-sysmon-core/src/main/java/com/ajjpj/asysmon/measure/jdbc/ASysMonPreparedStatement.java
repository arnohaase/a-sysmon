package com.ajjpj.asysmon.measure.jdbc;

import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.measure.ACollectingMeasurement;
import com.ajjpj.asysmon.measure.AMeasureCallback;
import com.ajjpj.asysmon.measure.AWithParameters;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;

/**
 * @author arno
 */
public class ASysMonPreparedStatement extends ASysMonStatement implements PreparedStatement {
    private static final int NUM_INT_TO_STRING = 20;
    private static final int NUM_BATCHES = 50;
    private static final String[] intToString = new String[NUM_BATCHES*NUM_INT_TO_STRING];
    static {
        for(int i=0; i<NUM_BATCHES; i++) {
            for(int j=0; j<NUM_INT_TO_STRING; j++) {
                intToString[i*NUM_BATCHES + j] = (i==0) ? String.valueOf(j) : "#" + i + ":" + j;
            }
        }
    }

    private final PreparedStatement inner;
    protected final ACollectingMeasurement m;

    protected int batchCount = 0;

    public ASysMonPreparedStatement(Connection conn, PreparedStatement inner, ASysMon sysMon, String sql) {
        super(conn, inner, sysMon);
        this.inner = inner;
        this.m = sysMon.startCollectingMeasurement(ident(sql));
    }

    //TODO it would be nice to call m.finish() if the connection is closed without the statement being closed first

    @Override public void close() throws SQLException {
        try {
            inner.close();
        }
        finally {
            m.finish();
        }
    }

    //--------------------------- execute

    @Override
    public ResultSet executeQuery() throws SQLException {
        return m.detail(ASysMonStatement.IDENT_EXECUTE, new AMeasureCallback<ResultSet, SQLException>() {
            @Override
            public ResultSet call(AWithParameters m) throws SQLException {
                return wrap(inner.executeQuery(), (ACollectingMeasurement) m);
            }
        });
    }

    @Override
    public int executeUpdate() throws SQLException {
        return m.detail(ASysMonStatement.IDENT_EXECUTE, new AMeasureCallback<Integer, SQLException>() {
            @Override public Integer call(AWithParameters m) throws SQLException {
                return inner.executeUpdate();
            }
        });
    }

    @Override
    public boolean execute() throws SQLException {
        return m.detail(ASysMonStatement.IDENT_EXECUTE, new AMeasureCallback<Boolean, SQLException>() {
            @Override public Boolean call(AWithParameters m) throws SQLException {
                return inner.execute();
            }
        });
    }


    //--------------------------- batch

    @Override
    public void addBatch() throws SQLException {
        batchCount += 1;
        inner.addBatch();
    }

    @Override
    public int[] executeBatch() throws SQLException {
        return m.detail(ASysMonStatement.IDENT_EXECUTE, new AMeasureCallback<int[], SQLException>() {
            @Override
            public int[] call(AWithParameters m) throws SQLException {
                return inner.executeBatch();
            }
        });
    }

    //--------------------------- setting parameters

    private String keyForIndex(int index) {
        // this lookup is more about memory consumption than about performance
        if(index < NUM_INT_TO_STRING && batchCount < NUM_BATCHES) {
            return intToString[batchCount * NUM_BATCHES + index];
        }

        return (batchCount == 0) ? String.valueOf(index) : "#" + batchCount + ":" + index;
    }

    private void setSysMonParam(int index, Object value) {
        m.addParameter(keyForIndex(index), String.valueOf(value));
    }

    @Override public void setNull(int parameterIndex, int sqlType) throws SQLException {
        setSysMonParam(parameterIndex, null);
        inner.setNull(parameterIndex, sqlType);
    }

    @Override public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setBoolean(parameterIndex, x);
    }

    @Override public void setByte(int parameterIndex, byte x) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setByte(parameterIndex, x);
    }

    @Override public void setShort(int parameterIndex, short x) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setShort(parameterIndex, x);
    }

    @Override public void setInt(int parameterIndex, int x) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setInt(parameterIndex, x);
    }

    @Override public void setLong(int parameterIndex, long x) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setLong(parameterIndex, x);
    }

    @Override public void setFloat(int parameterIndex, float x) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setFloat(parameterIndex, x);
    }

    @Override public void setDouble(int parameterIndex, double x) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setDouble(parameterIndex, x);
    }

    @Override public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setBigDecimal(parameterIndex, x);
    }

    @Override public void setString(int parameterIndex, String x) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setString(parameterIndex, x);
    }

    @Override public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setBytes(parameterIndex, x);
    }

    @Override public void setDate(int parameterIndex, Date x) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setDate(parameterIndex, x);
    }

    @Override public void setTime(int parameterIndex, Time x) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setTime(parameterIndex, x);
    }

    @Override public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setTimestamp(parameterIndex, x);
    }

    @Override public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setAsciiStream(parameterIndex, x, length);
    }

    @Override public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setUnicodeStream(parameterIndex, x, length);
    }

    @Override public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setBinaryStream(parameterIndex, x, length);
    }

    @Override public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setObject(parameterIndex, x, targetSqlType);
    }

    @Override public void setObject(int parameterIndex, Object x) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setObject(parameterIndex, x);
    }

    @Override public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        setSysMonParam(parameterIndex, reader);
        inner.setCharacterStream(parameterIndex, reader, length);
    }

    @Override public void setRef(int parameterIndex, Ref x) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setRef(parameterIndex, x);
    }

    @Override public void setBlob(int parameterIndex, Blob x) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setBlob(parameterIndex, x);
    }

    @Override public void setClob(int parameterIndex, Clob x) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setClob(parameterIndex, x);
    }

    @Override public void setArray(int parameterIndex, Array x) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setArray(parameterIndex, x);
    }

    @Override public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setDate(parameterIndex, x, cal);
    }

    @Override public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setTime(parameterIndex, x, cal);
    }

    @Override public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setTimestamp(parameterIndex, x, cal);
    }

    @Override public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        setSysMonParam(parameterIndex, null);
        inner.setNull(parameterIndex, sqlType, typeName);
    }

    @Override public void setURL(int parameterIndex, URL x) throws SQLException {
        setSysMonParam(parameterIndex, x);
        inner.setURL(parameterIndex, x);
    }

    @Override public void setRowId(int parameterIndex, RowId x) throws SQLException {
        setSysMonParam(parameterIndex, null);
        inner.setRowId(parameterIndex, x);
    }

    @Override public void setNString(int parameterIndex, String value) throws SQLException {
        setSysMonParam(parameterIndex, null);
        inner.setNString(parameterIndex, value);
    }

    @Override public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        setSysMonParam(parameterIndex, null);
        inner.setNCharacterStream(parameterIndex, value, length);
    }

    @Override public void setNClob(int parameterIndex, NClob value) throws SQLException {
        setSysMonParam(parameterIndex, null);
        inner.setNClob(parameterIndex, value);
    }

    @Override public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        setSysMonParam(parameterIndex, null);
        inner.setClob(parameterIndex, reader, length);
    }

    @Override public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        setSysMonParam(parameterIndex, null);
        inner.setBlob(parameterIndex, inputStream, length);
    }

    @Override public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        setSysMonParam(parameterIndex, null);
        inner.setNClob(parameterIndex, reader, length);
    }

    @Override public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        setSysMonParam(parameterIndex, null);
        inner.setSQLXML(parameterIndex, xmlObject);
    }

    @Override public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        inner.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    @Override public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        setSysMonParam(parameterIndex, null);
        inner.setAsciiStream(parameterIndex, x, length);
    }

    @Override public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        setSysMonParam(parameterIndex, null);
        inner.setBinaryStream(parameterIndex, x, length);
    }

    @Override public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        setSysMonParam(parameterIndex, null);
        inner.setCharacterStream(parameterIndex, reader, length);
    }

    @Override public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        setSysMonParam(parameterIndex, null);
        inner.setAsciiStream(parameterIndex, x);
    }

    @Override public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        setSysMonParam(parameterIndex, null);
        inner.setBinaryStream(parameterIndex, x);
    }

    @Override public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        setSysMonParam(parameterIndex, null);
        inner.setCharacterStream(parameterIndex, reader);
    }

    @Override public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        setSysMonParam(parameterIndex, null);
        inner.setNCharacterStream(parameterIndex, value);
    }

    @Override public void setClob(int parameterIndex, Reader reader) throws SQLException {
        setSysMonParam(parameterIndex, null);
        inner.setClob(parameterIndex, reader);
    }

    @Override public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        setSysMonParam(parameterIndex, null);
        inner.setBlob(parameterIndex, inputStream);
    }

    @Override public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        setSysMonParam(parameterIndex, null);
        inner.setNClob(parameterIndex, reader);
    }

    //----------------------- ignored by ASysMon

    @Override public void clearParameters() throws SQLException {
        inner.clearParameters();
    }

    @Override public ResultSetMetaData getMetaData() throws SQLException {
        return inner.getMetaData();
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return inner.getParameterMetaData();
    }
}
