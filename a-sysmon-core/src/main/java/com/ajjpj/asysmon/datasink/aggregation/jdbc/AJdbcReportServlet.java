package com.ajjpj.asysmon.datasink.aggregation.jdbc;

import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.datasink.aggregation.bottomup.ABottomUpLeafFilter;
import com.ajjpj.asysmon.datasink.aggregation.bottomup.ABottomUpReportServlet;
import com.ajjpj.asysmon.measure.jdbc.ASysMonStatement;


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
