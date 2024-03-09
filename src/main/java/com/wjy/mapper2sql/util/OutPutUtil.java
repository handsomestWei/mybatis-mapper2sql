package com.wjy.mapper2sql.util;

import java.util.Map;

import com.wjy.mapper2sql.bo.MapperSqlInfo;

/**
 * @author weijiayu
 * @date 2024/3/10 0:43
 */
public class OutPutUtil {

    public static void printf(MapperSqlInfo info) {
        System.out.println(String.format("---file=[%s], dbType=[%s], namespace=[%s]", info.getFileName(),
            info.getDbTypeName(), info.getNamespace()));
        System.out.println();
        for (Map.Entry<String, String> entry : info.getSqlIdMap().entrySet()) {
            System.out.println(String.format("---id=[%s]", entry.getKey()));
            System.out.println(entry.getValue());
            System.out.println();
        }
    }
}
