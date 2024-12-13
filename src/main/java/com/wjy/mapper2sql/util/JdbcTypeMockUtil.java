package com.wjy.mapper2sql.util;

import org.apache.ibatis.type.JdbcType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

/**
 * @author weijiayu
 * @date 2024/3/11 11:42
 */
public class JdbcTypeMockUtil {

    public static Object mockValue(JdbcType jdbcType) {
        switch (jdbcType) {
            case BOOLEAN:
                return new Random().nextBoolean();
            case CHAR:
            case VARCHAR:
            case ARRAY:
            case CLOB:
                return String.format("'%s'", UUID.randomUUID().toString().substring(0, 4));
            case SMALLINT:
            case INTEGER:
            case FLOAT:
            case DOUBLE:
            case BIGINT:
            case DECIMAL:
                return new Random().nextInt(10);
            case TIME:
            case TIMESTAMP:
            case DATE:
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timeStr = sdf.format(new Date());
                return String.format("'%s'", timeStr);
            default:
                return "''";
        }
    }
}
