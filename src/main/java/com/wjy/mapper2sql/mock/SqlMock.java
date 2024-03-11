package com.wjy.mapper2sql.mock;

import java.util.HashMap;

import org.apache.ibatis.type.JdbcType;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.wjy.mapper2sql.util.JdbcTypeMockUtil;

/**
 * @author weijiayu
 * @date 2024/3/10 1:42
 */
public class SqlMock {

    public static String mockSql(String sql, DbType dbType, String targetSymbol,
        HashMap<String, JdbcType> columnJdbcTypeMap) {
        return mockWithLineScan(sql, dbType, targetSymbol, columnJdbcTypeMap);
    }

    private static String mockWithLineScan(String sql, DbType dbType, String targetSymbol,
        HashMap<String, JdbcType> columnJdbcTypeMap) {
        sql = SQLUtils.format(sql, dbType);
        // TODO mock insert sql
        StringBuilder sb = new StringBuilder();
        String[] sqlPartArray = sql.split("\n");
        for (int i = 0; i < sqlPartArray.length; i++) {
            String sqlPart = sqlPartArray[i];
            sb.append(mockWithLineSplitScan(sqlPart, targetSymbol, columnJdbcTypeMap));
            if (i < sqlPartArray.length - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private static String mockWithLineSplitScan(String sql, String targetSymbol,
        HashMap<String, JdbcType> columnJdbcTypeMap) {
        if (!sql.contains(targetSymbol)) {
            return sql;
        }

        String columnName = null;
        JdbcType columnJdbcType = null;
        StringBuilder sb = new StringBuilder();
        String[] sqlPartArray = sql.split(" ");
        for (int i = 0; i < sqlPartArray.length; i++) {
            String sqlPart = sqlPartArray[i];
            if (i == 0) {
                if ("limit".equalsIgnoreCase(sqlPart) || "offset".equalsIgnoreCase(sqlPart)) {
                    columnJdbcType = JdbcType.INTEGER;
                }
            }
            if (columnJdbcType != null && sqlPart.contains(targetSymbol)) {
                Object mockValue = JdbcTypeMockUtil.mockValue(columnJdbcType);
                // TODO 暴力替换
                sb.append(sqlPart.replace(targetSymbol, mockValue.toString()));
            } else {
                sb.append(sqlPart);
                if (sqlPart.lastIndexOf(".") >= 0) {
                    // 子查询可能带别名，剔除前缀
                    sqlPart = sqlPart.substring(sqlPart.lastIndexOf(".") + 1);
                }
                if (columnJdbcTypeMap.containsKey(sqlPart)) {
                    // 表达式语法最近左匹配，更新置换符?最可能对应的类型
                    columnName = sqlPart;
                    columnJdbcType = columnJdbcTypeMap.get(columnName);
                }
            }

            if (i < sqlPartArray.length - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}
