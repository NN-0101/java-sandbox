package com.sandbox.services.db.mysql.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.util.Map;

/**
 * @description:
 * @author: 0101
 * @create: 2026/3/13
 */
@Slf4j
public class DataSourceUtil {

    private DataSourceUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static DataSource buildDataSource(Map<String, Object> dataSourceMap) {
        Object type = dataSourceMap.get("type");
        if (type == null) {
            type = "com.alibaba.druid.pool.DruidDataSource";
        }

        try {
            Class<? extends DataSource> dataSourceType = (Class<? extends DataSource>) Class.forName((String)type);
            String driverClassName = dataSourceMap.get("driver").toString();
            String url = dataSourceMap.get("url").toString();
            String username = dataSourceMap.get("username").toString();
            String password = dataSourceMap.get("password").toString();
            DataSourceBuilder<? extends DataSource> factory = DataSourceBuilder.create().url(url).username(username).password(password).type(dataSourceType).driverClassName(driverClassName);
            return factory.build();
        } catch (Exception var8) {
            log.error("构建数据源" + type + "出错", var8);
            return null;
        }
    }
}
