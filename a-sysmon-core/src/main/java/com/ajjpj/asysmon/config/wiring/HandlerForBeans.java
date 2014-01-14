package com.ajjpj.asysmon.config.wiring;

import com.ajjpj.asysmon.util.AOption;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author arno
 */
public class HandlerForBeans implements ConfigTypeHandler {
    @Override public boolean canHandle(Class<?> type, Class<?>[] paramTypes, String value) {
        return true;
    }

    @Override public Object handle(ConfigValueResolver r, String key, String value, Class<?> type, Class<?>[] paramTypes) throws Exception {
        final AOption<Class> explicitClass = classForName(value);
        if(explicitClass.isDefined()) {
            return getCreator(explicitClass.get(), 0, type, r).create();
        }

        return createFromChildRef(value, type, r);
    }

    private InstanceCreator getCreator(Class cls, int numParams, Class expectedType, ConfigValueResolver r) throws Exception {
        final AOption<ABeanFactory> beanFactory = beanFactory(cls);
        if(beanFactory.isDefined()) {
            return creatorFromBeanFactory(cls, beanFactory.get().factoryMethod(), numParams, expectedType, r);
        }
        return creatorFromExplicitClass(cls, numParams, r);

    }

    //TODO list instead of placeholder or fqn: expected type, list elements as ctor params

    private Object createFromChildRef(String value, Class type, ConfigValueResolver r) throws Exception {
        if(value.contains(".")) {
            r.throwConfigError("value is not a fully qualified class name: " + value);
        }

        // retrieve child element to get the class name
        final ConfigValueResolver childResolver = r.child(type, new Class<?>[0], value);
        final AOption<String> classNameRaw = childResolver.getRaw();
        if(classNameRaw.isEmpty()) {
            r.throwConfigError("no class name specified");
        }
        final AOption<Class> optChildClass = classForName(classNameRaw.get());
        if(optChildClass.isEmpty()) {
            r.throwConfigError(classNameRaw + " is not a valid class name");
        }

        final int numParams = getNumParams(childResolver);
        final InstanceCreator creator = getCreator(optChildClass.get(), numParams, type, childResolver);

        final Object[] params = new Object[numParams];
        for(int i=0; i<numParams; i++) {
            params[i] = childResolver.child(creator.getParameterTypes()[i], new Class<?>[0], String.valueOf(i)).get(AOption.none());
        }
        return creator.create(params);
    }

    private int getNumParams(ConfigValueResolver r) {
        for(int i=0; i<1000; i++) {
            final ConfigValueResolver paramResolver = r.child(null, null, String.valueOf(i));
            final AOption<String> param = paramResolver.getRaw();
            if(param.isEmpty()) {
                return i;
            }
        }
        return 1000;
    }

    private InstanceCreator creatorFromExplicitClass(Class cls, int numParams, ConfigValueResolver r) throws IllegalAccessException, InstantiationException, NoSuchMethodException {
        for(Constructor ctor: cls.getConstructors()) {
            if(ctor.getParameterTypes().length == numParams) {
                return new CtorInstanceCreator(ctor);
            }
        }

        r.throwConfigError("no constructor with " + numParams + " parameters");
        return null; // for the compiler
    }

    private InstanceCreator creatorFromBeanFactory(Class bfClass, String factoryMethodName, int numParams, Class expectedClass, ConfigValueResolver r) {
        for(Method candidate: bfClass.getMethods()) {
            if(!candidate.getName().equals(factoryMethodName)) {
                continue;
            }
            if(!Modifier.isStatic(candidate.getModifiers())) {
                continue;
            }
            if(candidate.getParameterTypes().length != numParams) {
                continue;
            }
            if(! expectedClass.isAssignableFrom(candidate.getReturnType())) {
                throw new IllegalArgumentException("incompatible return type " + candidate.getReturnType().getName());
            }
            return new FactoryMethodInstanceCreator(candidate);
        }
        r.throwConfigError("bean factory class " + bfClass.getName() + " has no public static factory method " + factoryMethodName + " with " + numParams + " parameters returning " + expectedClass.getName());
        return null; // for the compiler
    }

    private AOption<Class> classForName(String name) {
        try {
            return AOption.some((Class) Class.forName(name));
        }
        catch(Exception exc) {
            return AOption.none();
        }
    }

    private AOption<ABeanFactory> beanFactory(Class<?> cls) {
        return AOption.fromNullable(cls.getAnnotation(ABeanFactory.class));
    }

    private interface InstanceCreator {
        Class[] getParameterTypes();
        Object create(Object... params) throws Exception;
    }

    private class FactoryMethodInstanceCreator implements InstanceCreator {
        private final Method factoryMethod;

        private FactoryMethodInstanceCreator(Method factoryMethod) {
            this.factoryMethod = factoryMethod;
        }

        @Override public Class[] getParameterTypes() {
            return factoryMethod.getParameterTypes();
        }

        @Override public Object create(Object... params) throws Exception {
            return factoryMethod.invoke(null, params);
        }
    }

    private class CtorInstanceCreator implements InstanceCreator {
        private final Constructor ctor;

        private CtorInstanceCreator(Constructor ctor) {
            this.ctor = ctor;
        }

        @Override public Class[] getParameterTypes() {
            return ctor.getParameterTypes();
        }

        @Override public Object create(Object... params) throws Exception {
            return ctor.newInstance(params);
        }
    }
}
