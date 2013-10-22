package com.ajjpj.asysmon.render.minmaxavgservlet;

import com.ajjpj.asysmon.config.AStaticSysMonConfig;
import com.ajjpj.asysmon.processing.minmaxavg.AMinMaxAvgCollector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * //TODO documentation: load-on-startup = 1
 *
 * @author arno
 */
public class AMinMaxAvgServlet extends HttpServlet {
    private static volatile AMinMaxAvgCollector collector;

    /**
     * Override to customize initialization (and potentially registration) of the collector.
     */
    @Override public synchronized void init() throws ServletException {
        collector = new AMinMaxAvgCollector();
        AStaticSysMonConfig.addHandler(collector);
    }

    /**
     * All access to the collector is done through this method. Override to customize.
     */
    protected AMinMaxAvgCollector getCollector() {
        return collector;
    }

    @Override protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //TODO
    }
}
