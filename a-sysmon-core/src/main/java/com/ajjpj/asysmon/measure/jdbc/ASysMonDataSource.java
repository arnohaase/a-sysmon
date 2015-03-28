package com.ajjpj.asysmon.measure.jdbc;

import com.ajjpj.afoundation.function.AFunction0NoThrow;
import com.ajjpj.asysmon.ASysMonApi;
import com.ajjpj.asysmon.config.log.ASysMonLogger;
import com.ajjpj.asysmon.measure.ASimpleMeasurement;

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
    private final ASysMonApi sysMon;

    public ASysMonDataSource(DataSource inner, String poolIdentifier, ASysMonApi sysMon) {
        this.inner = inner;
        this.poolIdentifier = poolIdentifier;
        this.sysMon = sysMon;
    }

    @Override public Connection getConnection() throws SQLException {
        return new ASysMonConnection(inner.getConnection(), sysMon, poolIdentifier, new MeasuringConnectionCounter ());
    }

    @Override public Connection getConnection(String username, String password) throws SQLException {
        return new ASysMonConnection(inner.getConnection(username, password), sysMon, poolIdentifier, new MeasuringConnectionCounter ());
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

    /**
     * This measuring counter wraps a (potentially top-level) measurement around all code that is executed with the connection outside the pool. We do this
     *  only for connections from our own pool implementation because then we can be reasonably sure that 'close()' will be called when a chunk of work is finished.
     * If connections are created using the ASysMon driver, these connections may be pooled so that 'close()' is pretty much never called, and using the 'activation' /
     *  'passivation' mechanism seems a little fragile for this.
     */
    private class MeasuringConnectionCounter implements AIConnectionCounter {
        private final AIConnectionCounter inner = AConnectionCounter.INSTANCE; //TODO make this configurable?
        private final ASimpleMeasurement m;

        private MeasuringConnectionCounter () {
            m = sysMon.start (ASysMonStatement.IDENT_PREFIX_JDBC + "connection from pool");
            m.addParameter ("pool", poolIdentifier);
        }
        @Override public void onOpenConnection (String qualifier) {
            inner.onOpenConnection (qualifier);
        }
        @Override public void onCloseConnection (String qualifier) {
            try {
                m.finish ();
            }
            catch (final Exception exc) {
                ASysMonLogger.get (ASysMonDataSource.class).debug (new AFunction0NoThrow<String> () {
                    @Override public String apply () {
                        return "exception when finishing a JDBC connection measurement: " + exc;
                    }
                });
                // Silently ignore - this means that the fetching and closing of the connection did not happen at the same level in the call hierarchy. While such
                //  a symmetry is common and often desirable, it is by no means the only valid mode of using JDBC - so ASysMon needs to deal with it in a robust fashion.
            }
            inner.onCloseConnection (qualifier);
        }
        @Override public void onActivateConnection (String qualifier) {
            inner.onActivateConnection (qualifier);
        }
        @Override public void onPassivateConnection (String qualifier) {
            inner.onPassivateConnection (qualifier);
        }
    }
}
