package com.ajjpj.asysmon.config.wiring;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * @author arno
 */
class HandlerForPrimitives implements ConfigTypeHandler {
    private static final Set<Class<?>> types = new HashSet<Class<?>>(Arrays.asList(
            Boolean.class,
            Byte.class, Short.class, Character.class, Integer.class, Long.class,
            Float.class, Double.class,
            String.class
    ));

    @Override public boolean canHandle(Class<?> type, Class<?>[] paramTypes, String value) {
        return type.isPrimitive() || types.contains(type);
    }

    @Override public Object handle(ConfigValueResolver r, String key, String value, Class<?> type, Class<?>[] paramTypes) throws Exception {
        if(type == String.class) {
            return value;
        }

        if(type == Byte.class || type == Byte.TYPE) {
            return Byte.valueOf(value);
        }
        if(type == Short.class || type == Short.TYPE) {
            return Short.valueOf(value);
        }
        if(type == Character.class || type == Character.TYPE) {
            if(value.length() != 1) {
                r.throwConfigError("string value must have exactly one char to be coerced to character");
            }
            return value.charAt(0);
        }
        if(type == Integer.class || type == Integer.TYPE) {
            return Integer.valueOf(value);
        }
        if(type == Long.class || type == Long.TYPE) {
            return Long.valueOf(value);
        }
        if(type == Float.class || type == Float.TYPE) {
            return Float.valueOf(value);
        }
        if(type == Double.class || type == Double.TYPE) {
            return Double.valueOf(value);
        }
        if(type == Boolean.class || type == Boolean.TYPE) {
            return Boolean.valueOf(value);
        }

        r.throwConfigError("internal error - don't know how to handle type " + type.getName());
        return null; // for the compiler
    }
}
