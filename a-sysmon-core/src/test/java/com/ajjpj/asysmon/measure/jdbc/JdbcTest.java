package com.ajjpj.asysmon.measure.jdbc;

import com.ajjpj.asysmon.config.ADefaultSysMonConfig;
import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.measure.AMeasurementHierarchy;
import com.ajjpj.asysmon.testutil.CollectingDataSink;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;

/**
 * @author arno
 */
public class JdbcTest {
    @Test
    public void testTopLevelJdbc() throws Exception {
        //TODO refactor this test to use a sys mon holder to avoid hidden side effects through the default sysmon singleton
        final CollectingDataSink dataSink = new CollectingDataSink();
        ADefaultSysMonConfig.addHandler(dataSink);

        final Connection conn = DriverManager.getConnection("asysmon::jdbc:h2:mem:demo", "sa", "");
        final Statement stmt = conn.createStatement();
        stmt.execute("create table A (oid number primary key)");
        final ResultSet rs = conn.createStatement().executeQuery("select * from A");
        while(rs.next());
        stmt.close();
        conn.close();

        assertEquals(2, dataSink.data.size());
        assertEquals("jdbc: create table A (oid number primary key)", dataSink.data.get(0).getIdentifier());

        final AHierarchicalData selectRoot = dataSink.data.get(1);
        assertEquals(AMeasurementHierarchy.IDENT_SYNTHETIC_ROOT, selectRoot.getIdentifier());
        assertEquals(1, selectRoot.getChildren().size());
        assertEquals("jdbc: select * from A", selectRoot.getChildren().get(0).getIdentifier());
    }
    //TODO test implicit close of statement
    //TODO test implicit closing of nested measurements if parent measurement is closed
}
