package com.wjy.mapper2sql.util;

import org.apache.ibatis.type.JdbcType;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashMap;

/**
 * @author weijiayu
 * @date 2024/12/22 22:50
 */
public class MybatisUtil {

    public static HashMap<String, JdbcType> getTableColumnType(String tableName, Connection conn) {
        HashMap<String, JdbcType> columnJdbcTypeMap = new HashMap<>();
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            // 获取表信息
            // ResultSet resultSet = metaData.getTables(null, null, tableName, new String[] {"TABLE", "VIEW"});
            ResultSet resultSet = metaData.getColumns(null,null, tableName, null);
            while (resultSet.next()) {
                String columnName = resultSet.getString("COLUMN_NAME");
                // 将字段类型转化为mybatis中的jdbc类型
                int dataType = resultSet.getInt("DATA_TYPE");
                columnJdbcTypeMap.put(columnName, JdbcType.forCode(dataType));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return columnJdbcTypeMap;
    }
}
