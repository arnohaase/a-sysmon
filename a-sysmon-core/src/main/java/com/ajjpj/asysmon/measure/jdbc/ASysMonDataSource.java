package com.ajjpj.asysmon.measure.jdbc;

import com.ajjpj.asysmon.ASysMon;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * @author arno
 */
public class ASysMonDataSource implements DataSource {
    private final DataSource inner;

    private final String poolIdentifier;
    private final ASysMon sysMon;

    private final AConnectionCounter counter = AConnectionCounter.INSTANCE;

    public ASysMonDataSource(DataSource inner, String poolIdentifier, ASysMon sysMon) {
        this.inner = inner;
        this.poolIdentifier = poolIdentifier;
        this.sysMon = sysMon;
    }

    @Override public Connection getConnection() throws SQLException {
        return new ASysMonConnection(inner.getConnection(), sysMon, poolIdentifier, counter);
    }

    @Override public Connection getConnection(String username, String password) throws SQLException {
        return new ASysMonConnection(inner.getConnection(username, password), sysMon, poolIdentifier, counter);
    }

    @Override public PrintWriter getLogWriter() throws SQLException {
        return inner.getLogWriter();
    }

    @Override public void setLogWriter(PrintWriter out) throws SQLException {
        inner.setLogWriter(out);
    }

    @Override public void setLoginTimeout(int seconds) throws SQLException {
        inner.setLoginTimeout(seconds);
    }

    @Override public int getLoginTimeout() throws SQLException {
        return inner.getLoginTimeout();
    }

    @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return inner.getParentLogger();
    }

    @Override public <T> T unwrap(Class<T> iface) throws SQLException {
        return inner.unwrap(iface);
    }

    @Override public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return inner.isWrapperFor(iface);
    }
}
