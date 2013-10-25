package com.ajjpj.asysmon.datasink.jdbcreport;

import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.config.AStaticSysMonConfig;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


//TODO abstract servlet superclass
/**
 * @author arno
 */
public class AJdbcReportServlet extends HttpServlet {
    private static volatile AJdbcReportDataSink collector;

    /**
     * override to customize
     */
    @Override public void init() throws ServletException {
        synchronized (AJdbcReportServlet.class) {
            collector = new AJdbcReportDataSink();
            AStaticSysMonConfig.addHandler(collector);
        }
    }

    /**
     * override to customize
     */
    protected AJdbcReportDataSink getCollector() {
        return collector;
    }

    /**
     * Default implementations returns the singleton instance. Override to customize.
     */
    protected ASysMon getSysMon() {
        return ASysMon.get();
    }


    @Override protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("utf-8");
        final PrintWriter out = resp.getWriter();

        out.println("<html><head>");
        out.println("<title>A-SysMon JDBC overview</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>A-SysMon JDBC overview</h1>");

        for(AJdbcStatementData jdbcStatement: getCollector().getByJdbcStatement().values()) {
            out.println ("<div>" + jdbcStatement.getIdent() + ": " + jdbcStatement.getTotalNanos() + "</div>");
        }


        out.println("</body></html>");
    }
}
