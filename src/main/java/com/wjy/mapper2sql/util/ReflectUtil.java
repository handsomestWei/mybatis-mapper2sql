package com.wjy.mapper2sql.util;

import java.lang.reflect.Field;

/**
 * @author weijiayu
 * @date 2024/3/9 15:18
 */
public class ReflectUtil {

    public static void setFieldValueMaxDeep1(Object obj, String name, Object value)
        throws NoSuchFieldException, IllegalAccessException {
        Class clz = obj.getClass();
        Field field = null;
        try {
            field = clz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            // 第一次失败，再次尝试从父类获取。偷懒没有按具体类型判断。有的SqlNode类存在继承关系，主要操作的contents属性在父类。
            clz = clz.getSuperclass();
            field = clz.getDeclaredField(name);
        }
        field.setAccessible(true);
        field.set(obj, value);
    }

    public static Object getFieldValueMaxDeep1(Object obj, String name)
        throws NoSuchFieldException, IllegalAccessException {
        Class clz = obj.getClass();
        Field field = null;
        try {
            field = clz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            // 第一次失败，再次尝试从父类获取。偷懒没有按具体类型判断。有的SqlNode类存在继承关系，主要操作的contents属性在父类。
            // 例如TrimSqlNode类，有两个类SetSqlNode和WhereSqlNode继承它
            clz = clz.getSuperclass();
            field = clz.getDeclaredField(name);
        }
        field.setAccessible(true);
        return field.get(obj);
    }
}
