package com.ajjpj.asysmon.config.wiring;

import java.util.ArrayList;
import java.util.List;


/**
 * @author arno
 */
class HandlerForList implements ConfigTypeHandler {
    @Override public boolean canHandle(Class<?> type, Class<?>[] paramTypes, String value) {
        return type == List.class;
    }

    @Override public Object handle(ConfigValueResolver r, String key, String value, Class<?> type, Class<?>[] paramTypes) throws Exception {
        final List result = new ArrayList();

        if(value.isEmpty()) {
            return result;
        }

        final String[] elementsRaw = value.split(",");
        final ConfigValueResolver childResolver = r.child(paramTypes[0], new Class<?>[0]);
        for(String elRaw: elementsRaw) {
            result.add(childResolver.get(elRaw));
        }

        return result;
    }
}
