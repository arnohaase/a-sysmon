package com.ajjpj.asysmon;

import com.ajjpj.abase.function.AStatement0;
import com.ajjpj.asysmon.util.AShutdownable;
import com.ajjpj.abase.util.AUnchecker;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


/**
 * This listener takes care of shutting down the default A-SysMon instance on web application shutdown.
 *
 * @author arno
 */
public class AShutdownServletContextListener implements ServletContextListener {
    @Override public void contextDestroyed(ServletContextEvent sce) {
        AUnchecker.executeUnchecked(new AStatement0<Exception>() {
            @Override public void apply() throws Exception {
                ((AShutdownable) ASysMon.get()).shutdown();
            }
        });
    }

    @Override public void contextInitialized(ServletContextEvent sce) {
    }
}
