package com.ajjpj.asysmon.measure.jdbc;


import com.ajjpj.asysmon.ASysMon;

import java.sql.*;
import java.util.Properties;


/**
 * @author arno
 */
public class ASysMonJdbcDriver implements Driver {
    public static final String URL_PREFIX = "asysmon:";

    @Override public Connection connect(String url, Properties info) throws SQLException {
        final Connection inner = DriverManager.getConnection(url.substring(URL_PREFIX.length()), info);

        final ASysMon sysMon = ASysMon.get(); //TODO make this configurable - but how best to do that?!
        return new ASysMonConnection(inner, sysMon);
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
}
