package com.ajjpj.asysmon.servlet.jdbc;

import com.ajjpj.asysmon.ASysMonConfigurer;
import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.servlet.bottomup.ABottomUpDataSink;
import com.ajjpj.asysmon.servlet.bottomup.ABottomUpLeafFilter;
import com.ajjpj.asysmon.servlet.bottomup.ABottomUpReportServlet;
import com.ajjpj.asysmon.measure.jdbc.ASysMonStatement;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;


/**
 * @author arno
 */
public class AJdbcReportServlet extends ABottomUpReportServlet {
    private static volatile ABottomUpDataSink collector;

    @Override public void init(ServletConfig config) throws ServletException {
        super.init(config);

        synchronized (AJdbcReportServlet.class) {
            if(collector == null) {
                collector = new ABottomUpDataSink(createLeafFilter());
                ASysMonConfigurer.addDataSink(getSysMon(), collector);
            }
        }
    }

    /**
     * override to customize
     */
    protected ABottomUpLeafFilter createLeafFilter() {
        return new ABottomUpLeafFilter() {
            @Override public boolean isLeaf(AHierarchicalData data) {
                return data.getIdentifier().startsWith(ASysMonStatement.IDENT_PREFIX_JDBC);
            }
        };
    }

    @Override protected ABottomUpDataSink getCollector() {
        return collector;
    }

    @Override protected String getTitle() {
        return "A-SysMon JDBC report";
    }
}
