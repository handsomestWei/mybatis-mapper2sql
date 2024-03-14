package com.wjy.mapper2sql.bo;

import lombok.Data;

/**
 * @author weijiayu
 * @date 2024/3/14 10:26
 */
@Data
public class JdbcConnProperties {

    private String jdbcDriver;
    private String jdbcUrl;
    private String userName;
    private String password;

    public JdbcConnProperties(String jdbcDriver, String jdbcUrl, String userName, String password) {
        this.jdbcDriver = jdbcDriver;
        this.jdbcUrl = jdbcUrl;
        this.userName = userName;
        this.password = password;
    }
}
