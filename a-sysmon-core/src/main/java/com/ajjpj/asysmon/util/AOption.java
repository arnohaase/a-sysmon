package com.ajjpj.asysmon.util;

import java.util.NoSuchElementException;


/**
 * @author arno
 */
public abstract class AOption<T> { // do *not* move this class to a-base - this is only a light-weight copy
    public static <T> AOption<T> some(T el) {
        return new ASome<T>(el);
    }

    @SuppressWarnings("unchecked")
    public static <T> AOption<T> none() {
        return  (AOption<T>) ANone.INSTANCE;
    }

    public static <T> AOption<T> fromNullable(T nullable) {
        return nullable != null ? some(nullable) : AOption.<T>none();
    }

    public abstract boolean isDefined();
    public boolean isEmpty() {
        return !isDefined();
    }

    public abstract T get();
    public T getOrElse(T el) {
        return isDefined() ? get() : el;
    }

    public abstract boolean equals(Object o);
    public abstract int hashCode();

    static class ASome<T> extends AOption<T> {
        private final T el;

        ASome(T el) {
            this.el = el;
        }

        @Override public T get() {
            return el;
        }

        @Override public boolean isDefined() {
            return true;
        }

        @Override
        public String toString() {
            return "AOption.some(" + el + ")";
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ASome aSome = (ASome) o;

            if (el != null ? !el.equals(aSome.el) : aSome.el != null) return false;

            return true;
        }

        @Override public int hashCode() {
            return el != null ? el.hashCode() : 0;
        }
    }

    static class ANone extends AOption<Object> {
        public static final ANone INSTANCE = new ANone();

        private ANone() {}

        @Override public Object get() {
            throw new NoSuchElementException("no value for ANone");
        }

        @Override public boolean isDefined() {
            return false;
        }

        @Override
        public String toString() {
            return "AOption.none()";
        }

        @Override public boolean equals(Object o) {
            return o == this;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }
}


