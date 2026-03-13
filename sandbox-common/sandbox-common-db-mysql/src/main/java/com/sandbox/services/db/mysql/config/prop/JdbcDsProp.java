package com.sandbox.services.db.mysql.config.prop;

import lombok.Data;

/**
 * @description: 数据源配置属性类
 * @author: 0101
 * @create: 2026/3/13
 */
@Data
public class JdbcDsProp {

    private String jdbcUrl;
    private String username;
    private String password;
    private String type;
    private String driverClassName;
}
