package com.ajjpj.asysmon.datasink.aggregation.minmaxavg;

import com.ajjpj.asysmon.ASysMonConfigurer;
import com.ajjpj.asysmon.datasink.aggregation.AbstractDynamicAsysmonServlet;

import javax.servlet.ServletException;

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
}
