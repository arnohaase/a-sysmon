package com.ajjpj.asysmon.servlet.performance.bottomup;

import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.measure.jdbc.ASysMonStatement;


/**
 * @author arno
 */
public class AJdbcPageDefinition extends ABottomUpPageDefinition {
    @Override protected ABottomUpLeafFilter createLeafFilter() {
        return new ABottomUpLeafFilter() {
            @Override public boolean isLeaf(AHierarchicalData data) {
                return data.getIdentifier().startsWith(ASysMonStatement.IDENT_PREFIX_JDBC) &&
                        !data.getIdentifier ().startsWith (ASysMonStatement.IDENT_PREFIX_JDBC + "connection");
            }
        };
    }


    @Override public String getId() {
        return "jdbc";
    }

    @Override public String getShortLabel() {
        return "JDBC";
    }

    @Override public String getFullLabel() {
        return "JDBC Performance Statistics";
    }
}
