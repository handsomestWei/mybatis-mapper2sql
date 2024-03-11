package com.wjy.mapper2sql.parse.param;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author weijiayu
 * @date 2024/3/9 11:30
 */
public class SimpleSqlParamMap implements Map {
    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object o) {
        return false;
    }

    @Override
    public boolean containsValue(Object o) {
        return false;
    }

    @Override
    public Object get(Object o) {
        return new SimpleSqlParamMap();
    }

    @Override
    public Object put(Object o, Object o2) {
        return null;
    }

    @Override
    public Object remove(Object o) {
        return null;
    }

    @Override
    public void putAll(Map map) {}

    @Override
    public void clear() {}

    @Override
    public Set keySet() {
        return null;
    }

    @Override
    public Collection values() {
        return null;
    }

    @Override
    public Set<Map.Entry> entrySet() {
        Set<Entry> set = new HashSet<>();
        set.add(new Entry<Object, Object>() {
            @Override
            public Object getKey() {
                return "key";
            }

            @Override
            public Object getValue() {
                return "value";
            }

            @Override
            public Object setValue(Object o) {
                return null;
            }

            @Override
            public boolean equals(Object o) {
                return false;
            }

            @Override
            public int hashCode() {
                return 0;
            }
        });
        return set;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
