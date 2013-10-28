package com.ajjpj.asysmon.demo;

import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.measure.AMeasureCallback;
import com.ajjpj.asysmon.measure.ASimpleMeasurement;
import com.ajjpj.asysmon.measure.AWithParameters;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Random;

/**
 * @author arno
 */
public class AppServlet extends HttpServlet {
    static Connection conn;

    static {
        try {
            // store the connection to keep the in-memory database
            conn = getConnection();
            conn.createStatement().execute("create table A (oid number primary key)");
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
                final PreparedStatement ps = conn.prepareStatement("select * from A where oid < ?");
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
                conn.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("asysmon:qualifier=123:jdbc:h2:mem:demo", "sa", "");
    }
}
