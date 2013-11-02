package com.ajjpj.asysmon.measure.jdbc;

import com.ajjpj.asysmon.config.AStaticSysMonConfig;
import com.ajjpj.asysmon.testutil.CollectingDataSink;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import static org.junit.Assert.assertEquals;

/**
 * @author arno
 */
public class JdbcTest {
    @Test
    public void testTopLevelJdbc() throws Exception {
        final CollectingDataSink dataSink = new CollectingDataSink();
        AStaticSysMonConfig.addHandler(dataSink);

        final Connection conn = DriverManager.getConnection("asysmon::jdbc:h2:mem:demo", "sa", "");
        conn.createStatement().execute("create table A (oid number primary key)");
        final ResultSet rs = conn.createStatement().executeQuery("select * from A");
        while(rs.next());
        conn.close();

        assertEquals(2, dataSink.data.size());
        assertEquals("jdbc: create table A (oid number primary key)", dataSink.data.get(0).getIdentifier());
    }

    //TODO separate JDBC test from test for top-level collecting measurement
    //TODO test implicit close of statement
    //TODO test implicit closing of nested measurements if parent measurement is closed
}
