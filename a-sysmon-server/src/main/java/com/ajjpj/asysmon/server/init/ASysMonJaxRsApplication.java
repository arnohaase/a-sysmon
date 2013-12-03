package com.ajjpj.asysmon.server.init;

import com.ajjpj.asysmon.server.services.AdminService;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * @author arno
 */
public class ASysMonJaxRsApplication extends ResourceConfig {
    public ASysMonJaxRsApplication() {
        register(JacksonFeature.class);

        registerInstances(
                ASysMonModule.INJECTOR.getInstance(AdminService.class)
        );
    }
}
