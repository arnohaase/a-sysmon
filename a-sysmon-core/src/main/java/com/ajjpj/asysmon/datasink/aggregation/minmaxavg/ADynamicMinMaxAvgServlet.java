package com.ajjpj.asysmon.datasink.aggregation.minmaxavg;

import com.ajjpj.asysmon.ASysMonConfigurer;
import com.ajjpj.asysmon.datasink.aggregation.AbstractDynamicAsysmonServlet;

import javax.servlet.ServletException;
import java.util.Arrays;
import java.util.List;

/**
 * @author arno
 */
public class ADynamicMinMaxAvgServlet extends AbstractDynamicAsysmonServlet {
    private static volatile AMinMaxAvgDataSink collector;

    /**
     * Override to customize initialization (and potentially registration) of the collector.
     */
    @Override public void init() throws ServletException {
        synchronized (AMinMaxAvgServlet.class) {
            if(collector == null) {
                collector = createCollector();
                ASysMonConfigurer.addDataSink(getSysMon(), collector);
            }
        }
    }

    /**
     * Override to customize.
     */
    protected AMinMaxAvgDataSink createCollector() {
        return new AMinMaxAvgDataSink();
    }


    @Override
    protected String getTitle() {
        return "A-SysMon performance min / avg / max";
    }

    @Override protected boolean isStarted() {
        return collector.isActive();
    }

    @Override protected void doStartMeasurements() {
        collector.setActive(true);
    }

    @Override protected void doStopMeasurements() {
        collector.setActive(false);
    }

    @Override protected void doClearMeasurements() {
        collector.clear();
    }

    @Override protected List<ColDef> getColDefs() {
        return Arrays.asList(
                new ColDef("%",     true,  1, ColWidth.Medium),
                new ColDef("#",     false, 2, ColWidth.Medium),
                new ColDef("total", false, 0, ColWidth.Long),
                new ColDef("avg",   false, 0, ColWidth.Medium),
                new ColDef("min",   false, 0, ColWidth.Medium),
                new ColDef("max",   false, 0, ColWidth.Medium)
        );
    }
}
