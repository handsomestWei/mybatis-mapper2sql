package com.wjy.mapper2sql.util;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * @author weijiayu
 * @date 2024/3/14 10:34
 */
public class JdbcConnUtil {

    public static Connection newConnect(String jdbcDriver, String jdbcUrl, String userName, String password)
        throws Exception {
        Class.forName(jdbcDriver);
        return DriverManager.getConnection(jdbcUrl, userName, password);
    }
}
