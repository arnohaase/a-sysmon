package com.ajjpj.asysmon.datasink.aggregation.jdbc;

import com.ajjpj.asysmon.config.AStaticSysMonConfig;
import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.datasink.aggregation.AMinMaxAvgData;
import com.ajjpj.asysmon.datasink.aggregation.AbstractAsysmonServlet;
import com.ajjpj.asysmon.datasink.aggregation.bottomup.ABottomUpLeafFilter;
import com.ajjpj.asysmon.datasink.aggregation.bottomup.ABottomUpReportServlet;
import com.ajjpj.asysmon.measure.jdbc.ASysMonStatement;
import com.ajjpj.asysmon.util.APair;

import javax.servlet.ServletException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


//TODO abstract servlet superclass

/**
 * @author arno
 */
public class AJdbcReportServlet extends ABottomUpReportServlet {
    @Override public ABottomUpLeafFilter getLeafFilter() {
        return new ABottomUpLeafFilter() {
            @Override public boolean isLeaf(AHierarchicalData data) {
                return data.getIdentifier().startsWith(ASysMonStatement.IDENT_PREFIX_JDBC);
            }
        };
    }

    @Override protected String getTitle() {
        return "A-SysMon JDBC report";
    }
}
