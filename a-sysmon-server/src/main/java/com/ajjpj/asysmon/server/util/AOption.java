package com.ajjpj.asysmon.server.util;

/**
 * @author arno
 */
public abstract class AOption<T> {
    public static <T> AOption<T> some(T value) {
        return new ASome<T>(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> AOption<T> none() {
        return ANone.INSTANCE;
    }

    public static <T> AOption<T> fromNullable(T value) {
        if(value == null) {
            return none();
        }
        return some(value);
    }


    public abstract T get();
    public abstract boolean isDefined();

    public boolean isEmpty() {
        return ! isDefined();
    }

    public T getOrElse(T defaultValue) {
        if(isEmpty()) {
            return defaultValue;
        }
        return get();
    }
}

class ASome<T> extends AOption<T> {
    private final T value;

    ASome(T value) {
        this.value = value;
    }

    @Override public T get() {
        return value;
    }

    @Override public boolean isDefined() {
        return true;
    }
}

class ANone extends AOption {
    public static final ANone INSTANCE = new ANone();
    private ANone(){}

    @Override public Object get() {
        throw new IllegalStateException ("ANone has no value");
    }

    @Override public boolean isDefined() {
        return false;
    }
}