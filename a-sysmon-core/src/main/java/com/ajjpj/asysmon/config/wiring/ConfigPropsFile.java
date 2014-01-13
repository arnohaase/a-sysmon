package com.ajjpj.asysmon.config.wiring;

import com.ajjpj.asysmon.config.log.ASysMonLogger;
import com.ajjpj.asysmon.util.AOption;
import com.ajjpj.asysmon.util.AUnchecker;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author arno
 */
public class ConfigPropsFile {
    private final Properties props;
    private final ASysMonLogger log;

    public ConfigPropsFile(Properties props, ASysMonLogger log) {
        this.props = props;
        this.log = log;
    }

    public String get(String key, boolean expected) {
        final String result = props.getProperty(key);
        if(expected && result == null) {
            log.warn("no configuration for property " + key + ", assuming empty");
            return "";
        }
        return result. trim();
    }

    public String[] getAndSplit(String key, boolean expected) {
        final String raw = get(key, expected).trim();
        if(raw.isEmpty()) {
            return new String[0];
        }
        return raw.split(",");
    }

    public <T> List<T> createInstances(String key, Class<T> superClass) {
        final List<T> result = new ArrayList<T>();

        try {
            for(String segRaw: getAndSplit(key, true)) {
                final AOption<T> instance = createInstance(key, segRaw, superClass);
                if(instance.isDefined()) {
                    result.add(instance.get());
                }
            }
        } catch (Exception e) {
            AUnchecker.throwUnchecked(e);
        }

        return result;
    }

    public <T> AOption<T> createInstance(String key, String nameRaw, Class<T> superClass) throws Exception {
        final String name = nameRaw.trim();
        if(name.isEmpty()) {
            log.warn("no content between commas in configuration '" + props.getProperty(key) + "' for key '" + key + "', ignoring empty segments");
            return AOption.none();
        }

        final AOption<Class<? extends T>> explicitClass = asClass(name, superClass, key);
        if(explicitClass.isDefined()) {
            return AOption.some(instantiate(explicitClass.get(), superClass, key));
        }

        if(name.contains(".")) {
            throw new IllegalArgumentException("configuration error: value " + name + " is not a valid class name (key: " + key + ")");
        }

        final String instanceKey = key + "." + name;
        final String instanceValue = props.getProperty(instanceKey);
        if(instanceValue == null) {
            throw new IllegalArgumentException("config error: there is no property " + instanceKey);
        }

        final AOption<Class<? extends T>> indirectClass = asClass(instanceValue.trim(), superClass, key);
        if(indirectClass.isEmpty()) {
            throw new IllegalArgumentException("configuration error: value " + instanceValue + " is not a valid class name (key: " + instanceKey + ")");
        }

        final List<Object> params = new ArrayList<Object>();
        for(int i=0; i< 1000; i++) {
            final String paramKey = instanceKey + "." + i;
            if(props.containsKey(paramKey)) {
                params.add(props.getProperty(paramKey).trim());
            }
        }

        return AOption.some(instantiate(indirectClass.get(), superClass, key, params.toArray()));
    }

    @SuppressWarnings("unchecked")
    private <T> T instantiate(Class<T> cls, Class<?> superClass, String key, Object... params) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        final ABeanFactory beanFactoryAnnotation = cls.getAnnotation(ABeanFactory.class);
        if(beanFactoryAnnotation != null) {
            final String factoryMethodName = beanFactoryAnnotation.factoryMethod();
            final Method factoryMethod = getFactoryMethod(cls, factoryMethodName, params.length, superClass, key);

            return (T) factoryMethod.invoke(null, params);
        }

        final Constructor<T> ctor = getNArgsConstructor(cls, params.length);
        final Object[] coerced = coerceParams(ctor.getParameterTypes(), params, cls);
        return ctor.newInstance(coerced);
    }

    private Method getFactoryMethod(Class<?> cls, String methodName, int numParams, Class<?> superClass, String key) {
        for(Method candidate: cls.getMethods()) {
            if(! candidate.getName().equals(methodName)) {
                continue;
            }
            if(candidate.getParameterTypes().length != numParams) {
                continue;
            }
            if(! superClass.isAssignableFrom(candidate.getReturnType())) {
                continue;
            }
            if(!Modifier.isStatic(candidate.getModifiers())) {
                continue;
            }
            return candidate;
        }
        throw new IllegalArgumentException("configuration error: class " + cls.getName() + " does not have a public static method " + methodName + " that takes " + numParams + " arguments and returns " + superClass.getName() + " (key " + key + ")");
    }

    private Object[] coerceParams(Class<?>[] types, Object[] params, Class<?> beanClass) throws NoSuchFieldException, IllegalAccessException {
        for(int i=0; i<params.length; i++) {
            params[i] = coerce(types[i], params[i], beanClass, i);
        }
        return params;
    }

    @SuppressWarnings("unchecked")
    private Object coerce(Class<?> type, Object value, Class<?> beanClass, int idx) throws NoSuchFieldException, IllegalAccessException {
        if(value == null || type.isInstance(value)) {
            return value;
        }
        if(value instanceof String) {
            final String s = (String) value;

            try {
                final Field f = type.getField(s);
                if(f != null && type.isAssignableFrom(f.getType()) && Modifier.isStatic(f.getModifiers())) {
                    return f.get(null);
                }
            }
            catch(Exception exc) {
            }

            if(Enum.class.isAssignableFrom(type)) {
                return Enum.valueOf((Class<? extends Enum>) type, s);
            }
            if(type == Byte.class || type == Byte.TYPE) {
                return Byte.valueOf(s);
            }
            if(type == Short.class || type == Short.TYPE) {
                return Short.valueOf(s);
            }
            if(type == Character.class || type == Character.TYPE) {
                if(s.length() != 1) {
                    throw new IllegalArgumentException("could not coerce '" + s + "' to char (index " + idx + " for class " + beanClass.getName() + ")");
                }
                return s.charAt(0);
            }
            if(type == Integer.class || type == Integer.TYPE) {
                return Integer.valueOf(s);
            }
            if(type == Long.class || type == Long.TYPE) {
                return Long.valueOf(s);
            }
            if(type == Float.class || type == Float.TYPE) {
                return Float.valueOf(s);
            }
            if(type == Double.class || type == Double.TYPE) {
                return Double.valueOf(s);
            }
            if(type == Boolean.class || type == Boolean.TYPE) {
                return Boolean.valueOf(s);
            }
        }
        throw new IllegalArgumentException("could not coerce " + value + " of type " + value.getClass().getName() + " to type " + type.getName() + " (index " + idx + " for class " + beanClass.getName() + ")");
    }

    @SuppressWarnings("unchecked")
    private <T> Constructor<T> getNArgsConstructor(Class<T> cls, int numArgs) {
        for(Constructor ctor: cls.getConstructors()) {
            if(ctor.getParameterTypes().length == numArgs) {
                return ctor;
            }
        }
        throw new IllegalArgumentException("class " + cls.getName() + " does not have a constructor with " + numArgs + " arguments");
    }

    @SuppressWarnings("unchecked")
    private <T> AOption<Class<? extends T>> asClass(String fqn, Class<T> superClass, String key) {
        try {
            final Class result = Thread.currentThread().getContextClassLoader().loadClass(fqn);
            if(! superClass.isAssignableFrom(result)) {
                throw new IllegalArgumentException("configuration error: class " + fqn + " does not extend " + superClass.getName() + " (key " + key + ")");
            }

            return (AOption) AOption.some(result);
        } catch (Exception e) {
            return AOption.none();
        }
    }
}
