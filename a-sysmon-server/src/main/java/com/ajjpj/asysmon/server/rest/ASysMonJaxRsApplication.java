package com.ajjpj.asysmon.server.rest;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * @author arno
 */
public class ASysMonJaxRsApplication extends Application {
//    @Override
//    public Set<Class<?>> getClasses() {
//        final Set<Class<?>> result = new HashSet<>();
//        result.add(DummyJsonService.class);
//        return result;
//    }

    @Override
    public Set<Object> getSingletons() {
        final Set<Object> result = new HashSet<>();
        result.add(new DummyJsonService());
        return result;
    }
}
