package com.ajjpj.asysmon.measure.jdbc;


import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.measure.ASysMonSource;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;


/**
 * @author arno
 */
public class ASysMonJdbcDriver implements Driver {
    public static final String URL_PREFIX = "asysmon:";

    public static final String PARAM_CONNECTIONPOOL_IDENTIFIER = "qualifier";
    public static final String PARAM_SYSMON_SOURCE = "sysmon-source";

    public static final ASysMonJdbcDriver INSTANCE = new ASysMonJdbcDriver();

    static {
        try {
            DriverManager.registerDriver(INSTANCE);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deregister() throws SQLException {
        DriverManager.deregisterDriver(INSTANCE);
    }

    @Override public Connection connect(String url, Properties info) throws SQLException {
        if(! acceptsURL(url)) {
            return null;
        }

        final String withoutPrefix = url.substring(URL_PREFIX.length());
        final int idxColon = withoutPrefix.indexOf(':');
        if(idxColon == -1) {
            return null;
        }
        final String paramString = withoutPrefix.substring(0, idxColon);
        final Map<String, String> params = parseParams(paramString);

        final String innerUrl = withoutPrefix.substring(idxColon+1);
        final Connection inner = DriverManager.getConnection(innerUrl, info);

        final ASysMon sysMon = getSysMon(params);
        return new ASysMonConnection(inner, sysMon, getPoolIdentifier(params), AConnectionCounter.INSTANCE); //TODO make instance management configurable
    }

    private String getPoolIdentifier(Map<String, String> params) {
        return params.get(PARAM_CONNECTIONPOOL_IDENTIFIER);
    }

    private ASysMon getSysMon(Map<String, String> params) throws SQLException {
        final String sysmonSourceName = params.get(PARAM_SYSMON_SOURCE);
        if(sysmonSourceName == null) {
            return ASysMon.get();
        }

        try {
            final ASysMonSource sysMonSource = (ASysMonSource) Class.forName(sysmonSourceName).newInstance();
            return sysMonSource.getSysMon();
        } catch (Exception exc) {
            throw new SQLException("error retrieving ASysMon instance", exc);
        }
    }

    private Map<String, String> parseParams(String paramString) {
        final Map<String, String> result = new HashMap<String, String>();

        for(String part: paramString.split(";")) {
            final String[] keyValue = part.split("=");
            if(keyValue.length != 2) {
                throw new IllegalArgumentException("key/value pairs with '=', ';' between pairs");
            }
            result.put(keyValue[0].toLowerCase(), keyValue[1]);
        }
        return result;
    }

    @Override public boolean acceptsURL(String url) throws SQLException {
        return url != null && url.startsWith(URL_PREFIX);
    }

    @Override public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public int getMajorVersion() {
        return 1;
    }

    @Override public int getMinorVersion() {
        return 0;
    }

    @Override public boolean jdbcCompliant() {
        return true; //TODO what to return here?!
    }

    // introduced with JDK 1.7
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
}
