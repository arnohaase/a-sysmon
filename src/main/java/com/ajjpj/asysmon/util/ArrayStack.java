package com.ajjpj.asysmon.util;


import java.util.NoSuchElementException;

/**
 * This is a mutable array-based implementation of a stack.
 *
 * @author arno
 */
public class ArrayStack<T> {
    private T[] data;
    private int size = 0;

    public ArrayStack() {
        this(10);
    }

    @SuppressWarnings("unchecked")
    public ArrayStack(int initialSize) {
        if(initialSize <= 0) {
            throw new IllegalArgumentException("size must be greater than 0");
        }

        data = (T[]) new Object[initialSize];
    }

    @SuppressWarnings("unchecked")
    public void push(T el) {
        if(size >= data.length) {
            final T[] oldData = data;
            data = (T[]) new Object[2*oldData.length];
            System.arraycopy(oldData, 0, data, 0, oldData.length);
        }

        data[size] = el;
        size += 1;
    }

    public T pop() {
        if(isEmpty()) {
            throw new NoSuchElementException("stack is empty");
        }
        final T result = data[size-1];
        data[size-1] = null; // allow GC of previous element
        size -= 1;
        return result;
    }

    public T peek() {
        if(isEmpty()) {
            throw new NoSuchElementException("stack is empty");
        }
        return data[size-1];
    }

    //TODO tryPop, tryPeek

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }
    public boolean nonEmpty() {
        return size > 0;
    }
}
