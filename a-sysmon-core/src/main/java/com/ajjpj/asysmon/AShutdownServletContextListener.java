package com.ajjpj.asysmon;

import com.ajjpj.asysmon.config.AGlobalConfig;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


/**
 * This listener takes care of shutting down the default A-SysMon instance on web application shutdown.
 *
 * @author arno
 */
public class AShutdownServletContextListener implements ServletContextListener {
    @Override public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("shutting down");
        if(AGlobalConfig.getImplicitlyShutDownWithServlet()) {
            ASysMon.get().shutdown();
        }
    }

    @Override public void contextInitialized(ServletContextEvent sce) {
        System.out.println("context initialized");
    }
}
