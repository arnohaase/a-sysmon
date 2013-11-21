package com.ajjpj.asysmon.demo;

import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.measure.AMeasureCallback;
import com.ajjpj.asysmon.measure.ASimpleMeasurement;
import com.ajjpj.asysmon.measure.AWithParameters;
import com.ajjpj.asysmon.measure.jdbc.AConnectionCounter;
import com.ajjpj.asysmon.measure.jdbc.ASysMonDataSource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

/**
 * @author arno
 */
public class AppServlet extends HttpServlet {
    private static final DataSource dataSource = createDataSource();

    static Connection conn;

    static {
        try {
            // store the connection to keep the in-memory database
            conn = getConnection();
            conn.createStatement().execute("create table A (oid number primary key)");

            conn.commit();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final PrintWriter out = resp.getWriter();
        out.println ("<html><head><title>A-SysMon demo content</title></head><body><h1>A-SysMon demo content</h1></body></html>");

        final ASimpleMeasurement parMeasurement = ASysMon.get().start("parallel", false);

        sleep();

        ASysMon.get().measure("a", new AMeasureCallback<Object, RuntimeException>() {
            @Override
            public Object call(AWithParameters m) {
                return sleep();
            }
        });
        ASysMon.get().measure("b", new AMeasureCallback<Object, RuntimeException>() {
            @Override
            public Object call(AWithParameters m) {
                doQuery(); doQuery(); doQuery(); return sleep();
            }
        });
        ASysMon.get().measure("q", new AMeasureCallback<Object, RuntimeException>() {
            @Override
            public Object call(AWithParameters m) {
                doQuery(); doQuery(); doQuery(); doQuery(); doQuery(); doQuery(); doQuery(); doQuery(); return sleep();
            }
        });
        ASysMon.get().measure("b", new AMeasureCallback<Object, RuntimeException>() {
            @Override
            public Object call(AWithParameters m) {
                return sleep();
            }
        });
        ASysMon.get().measure("b", new AMeasureCallback<Object, RuntimeException>() {
            @Override
            public Object call(AWithParameters m) {
                return sleep();
            }
        });
        ASysMon.get().measure("b", new AMeasureCallback<Object, RuntimeException>() {
            @Override
            public Object call(AWithParameters m) {
                return sleep();
            }
        });
        ASysMon.get().measure("b", new AMeasureCallback<Object, RuntimeException>() {
            @Override
            public Object call(AWithParameters m) {
                return sleep();
            }
        });
        ASysMon.get().measure("a", new AMeasureCallback<Object, RuntimeException>() {
            @Override
            public Object call(AWithParameters m) {
                return sleep();
            }
        });
        ASysMon.get().measure("a", new AMeasureCallback<Object, RuntimeException>() {
            @Override
            public Object call(AWithParameters m) {
                doQuery(); return sleep();
            }
        });
        ASysMon.get().measure("b", new AMeasureCallback<Object, RuntimeException>() {
            @Override
            public Object call(AWithParameters m) {
                ASysMon.get().measure("x", new AMeasureCallback<Object, RuntimeException>() {
                    @Override
                    public Object call(AWithParameters m) {
                        doQuery(); return sleep();
                    }
                });
                doQuery(); return sleep();
            }
        });
        parMeasurement.finish();
        ASysMon.get().measure("c", new AMeasureCallback<Object, RuntimeException>() {
            @Override
            public Object call(AWithParameters m) {
                return sleep();
            }
        });
    }

    private Object sleep() {
        try {
            Thread.sleep(20 + new Random().nextInt(20));
        } catch (InterruptedException exc) {
            throw new RuntimeException(exc);
        }
        return null;
    }

    private void doQuery() {
        try {
            final Connection conn = getConnection();
            try {
                final PreparedStatement ps = conn.prepareStatement("select * from A where oid < ? and 1=1 and 1=1 and 1=1 and 1=1 and 1=1 and 1=1 and 1=1 and 1=1 and 1=1 and 1=1 and 1=1 and 1=1 and 1=1 and 1=1 and 1=1 and 1=1 and 1=1 and 1=1 and 1=1 and 1=1 and 1=1 and 1=1 and 1=1 and 1=1 and 1=1 and 1=1 and 1=1");
                try {
                    ps.setLong(1, 25);
                    final ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                    }
                }
                finally {
                    ps.close();
                }
            } finally {
                conn.commit();
                conn.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
//        final Connection result = DriverManager.getConnection("asysmon:qualifier=123:jdbc:h2:mem:demo", "sa", "");
//        result.setAutoCommit(false);
//        return result;
    }

    private static DataSource createDataSource() {
        final DataSource inner = new DataSource() {
            @Override public Connection getConnection() throws SQLException {
                final Connection result = DriverManager.getConnection("jdbc:h2:mem:demo", "sa", "");
                result.setAutoCommit(false);
                return result;
            }

            @Override
            public Connection getConnection(String username, String password) throws SQLException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public PrintWriter getLogWriter() throws SQLException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setLogWriter(PrintWriter out) throws SQLException {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setLoginTimeout(int seconds) throws SQLException {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getLoginTimeout() throws SQLException {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Logger getParentLogger() throws SQLFeatureNotSupportedException {
                return null;
            }

            @Override
            public <T> T unwrap(Class<T> iface) throws SQLException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isWrapperFor(Class<?> iface) throws SQLException {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };

        return new ASysMonDataSource(inner, "234", ASysMon.get());
    }
}
