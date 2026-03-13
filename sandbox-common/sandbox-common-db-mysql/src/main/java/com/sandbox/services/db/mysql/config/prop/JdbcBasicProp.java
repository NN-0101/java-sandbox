package com.sandbox.services.db.mysql.config.prop;

import lombok.Data;

/**
 * @description: 数据库连接池性能优化配置类
 * @author: 0101
 * @create: 2026/3/13
 */
@Data
public class JdbcBasicProp {
    private String maxActive;
    private String minIdle;
    private String initialSize;
    private String logAbandoned;
    private String removeAbandoned;
    private String removeAbandonedTimeout;
    private String maxWait;
    private String timeBetweenEvictionRunsMillis;
    private String numTestsPerEvictionRun;
    private String minEvictableIdleTimeMillis;
    private String validationQuery;
    private String testWhileIdle;
    private String testOnBorrow;
    private String testOnReturn;
}
