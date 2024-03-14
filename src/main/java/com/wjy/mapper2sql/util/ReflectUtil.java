package com.wjy.mapper2sql.util;

import java.lang.reflect.Field;

/**
 * @author weijiayu
 * @date 2024/3/9 15:18
 */
public class ReflectUtil {

    public static void setFieldValue(boolean isSuper, Object obj, String name, Object value)
        throws NoSuchFieldException, IllegalAccessException {
        Class clz = obj.getClass();
        if (isSuper) {
            clz = clz.getSuperclass();
        }
        Field field = clz.getDeclaredField(name);
        field.setAccessible(true);
        field.set(obj, value);
    }

    public static Object getFieldValue(boolean isSuper, Object obj, String name)
        throws NoSuchFieldException, IllegalAccessException {
        Class clz = obj.getClass();
        if (isSuper) {
            clz = clz.getSuperclass();
        }
        Field field = clz.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(obj);
    }
}
