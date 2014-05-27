package com.ajjpj.asysmon.measure.jdbc;

import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.data.AHierarchicalDataRoot;
import com.ajjpj.asysmon.impl.ASysMonConfigurer;
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
        ASysMonConfigurer.addDataSink(ASysMon.get(), dataSink);

        final Connection conn = DriverManager.getConnection("asysmon::jdbc:h2:mem:demo", "sa", "");
        final Statement stmt = conn.createStatement();
        stmt.execute("create table A (oid number primary key)");
        stmt.execute("insert into A (oid) values (1)");
        final ResultSet rs = conn.createStatement().executeQuery("select * from A");
        while(rs.next());
        rs.close();
        stmt.close();
        conn.close();

        assertEquals(3, dataSink.data.size());
        assertEquals("jdbc: create table A (oid number primary key)", dataSink.data.get(0).getRootNode().getIdentifier());
        assertEquals("jdbc: insert into A (oid) values (1)", dataSink.data.get(1).getRootNode().getIdentifier());

        final AHierarchicalData selectRoot = dataSink.data.get(2).getRootNode();
        assertEquals(AMeasurementHierarchy.IDENT_SYNTHETIC_ROOT, selectRoot.getIdentifier());
        assertEquals(1, selectRoot.getChildren().size());
        assertEquals("jdbc: select * from A", selectRoot.getChildren().get(0).getIdentifier());
    }

    //TODO implicit close of rs and stmt --> how do connection pools deal with this? They do not call Connection.close()...
    //TODO test detail measurements of SELECT
}
