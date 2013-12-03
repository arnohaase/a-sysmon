package com.ajjpj.asysmon.server.rest;

import com.ajjpj.asysmon.server.ASysMonModule;
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
                new DummyJsonService(),
                ASysMonModule.INJECTOR.getInstance(AdminService.class)
        );
    }
}
