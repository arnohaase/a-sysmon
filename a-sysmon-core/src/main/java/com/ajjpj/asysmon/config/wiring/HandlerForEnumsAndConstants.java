package com.ajjpj.asysmon.config.wiring;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;


/**
 * @author arno
 */
class HandlerForEnumsAndConstants implements ConfigTypeHandler {
    @Override public boolean canHandle(Class<?> type, Class<?>[] paramTypes, String value) {
        try {
            final Field f = type.getField(value);
            if(!Modifier.isStatic(f.getModifiers())) {
                return false;
            }
            return type.isAssignableFrom(f.getType());
        }
        catch(Exception exc) {
            return false;
        }
    }

    @Override public Object handle(ConfigValueResolver r, String key, String value, Class<?> type, Class<?>[] paramTypes) throws Exception {
        return type.getField(value).get(null);
    }
}
