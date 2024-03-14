package com.wjy.mapper2sql.parse.param;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 无限套娃，适配ognl表达式检测
 * 
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
        return generateSet();
    }

    @Override
    public boolean equals(Object o) {
        // TODO
        // 类型相等，参数绑定时会被设值为全限定类名@数字，例com.wjy.mapper2sql.parse.param.SimpleSqlParamMap@1
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    private Set generateSet() {
        Set<Entry> set = new HashSet<>();
        set.add(new Entry<Object, Object>() {
            @Override
            public Object getKey() {
                return new SimpleSqlParamMap();
            }

            @Override
            public Object getValue() {
                return new SimpleSqlParamMap();
            }

            @Override
            public Object setValue(Object o) {
                return new SimpleSqlParamMap();
            }

            @Override
            public boolean equals(Object o) {
                return true;
            }

            @Override
            public int hashCode() {
                return 0;
            }
        });
        return set;
    }
}
