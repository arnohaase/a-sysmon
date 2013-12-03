package com.ajjpj.asysmon.server.util.json;


import java.util.List;

/**
 * This wrapper is necessary to serialize a list to JSON: arrays are not permitted at the top level.
 *
 * @author arno
 */
public class ListWrapper<T> {
    private final List<T> data;

    public ListWrapper(List<T> data) {
        this.data = data;
    }

    public List<T> getData() {
        return data;
    }

    @Override
    public String toString() {
        return "ListWrapper{" +
                "data=" + data +
                '}';
    }
}
