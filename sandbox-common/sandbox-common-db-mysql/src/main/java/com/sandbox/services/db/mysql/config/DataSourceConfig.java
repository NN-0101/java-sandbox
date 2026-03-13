package com.sandbox.services.db.mysql.config;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.collect.Lists;
import com.sandbox.services.db.mysql.config.prop.JdbcBasicProp;
import com.sandbox.services.db.mysql.config.prop.JdbcDsProp;
import com.sandbox.services.db.mysql.util.DataSourceUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: 数据源配置优化版
 * @author: 0101
 * @create: 2026/3/13
 */
@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "sharding")
public class DataSourceConfig {

    private JdbcDsProp ds0;
    private JdbcDsProp ds1;
    private JdbcDsProp ds0slave0;
    private JdbcDsProp ds1slave0;
    private JdbcBasicProp basic;

    @PostConstruct
    public void init() {
        log.info("DataSourceConfig initialized with basic properties: {}", basic);
    }

    /**
     * druid数据源0
     */
    public DataSource ds0() {
        return createDatasource(getDs0(), "ds0");
    }

    /**
     * druid数据源1
     */
    public DataSource ds1() {
        return createDatasource(getDs1(), "ds1");
    }

    /**
     * druid数据源0备库
     */
    public DataSource ds0slave0() {
        return createDatasource(getDs0slave0(), "ds0slave0");
    }

    /**
     * druid数据源1备库
     */
    public DataSource ds1slave0() {
        return createDatasource(getDs1slave0(), "ds1slave0");
    }

    private DataSource createDatasource(JdbcDsProp jdbcdsProp, String dsName) {
        long startTime = System.currentTimeMillis();

        Map<String, Object> dsMap = new HashMap<>();
        dsMap.put("type", jdbcdsProp.getType());
        dsMap.put("url", jdbcdsProp.getJdbcUrl());
        dsMap.put("driver", jdbcdsProp.getDriverClassName());
        dsMap.put("username", jdbcdsProp.getUsername());
        dsMap.put("password", jdbcdsProp.getPassword());

        DruidDataSource ds = (DruidDataSource) DataSourceUtil.buildDataSource(dsMap);
        if (ds == null) {
            log.error("Failed to create datasource: {}", dsName);
            throw new RuntimeException("Failed to create datasource: " + dsName);
        }

        // 设置数据源名称，方便监控
        ds.setName(dsName);

        // 初始化基础属性
        initBasicProperties(ds, dsName);

        long cost = System.currentTimeMillis() - startTime;
        log.info("Datasource {} created, cost: {}ms", dsName, cost);

        return ds;
    }

    private void initBasicProperties(DruidDataSource ds, String dsName) {
        try {
            // 基础连接池配置
            ds.setMaxActive(Integer.parseInt(basic.getMaxActive()));
            ds.setMinIdle(Integer.parseInt(basic.getMinIdle()));

            // 关键优化1: 降低初始连接数，加快启动速度
            int initialSize = Integer.parseInt(basic.getInitialSize());
            String env = System.getProperty("spring.profiles.active", "dev");
            if ("dev".equals(env) || "test".equals(env)) {
                initialSize = Math.min(initialSize, 1);
            }
            ds.setInitialSize(initialSize);

            // 连接等待配置
            ds.setMaxWait(Integer.parseInt(basic.getMaxWait()));

            // 连接泄漏检测配置
            boolean removeAbandoned = Boolean.parseBoolean(basic.getRemoveAbandoned());
            if (removeAbandoned) {
                log.warn("{}: removeAbandoned is true, not recommended in production", dsName);
                ds.setRemoveAbandonedTimeout(Integer.parseInt(basic.getRemoveAbandonedTimeout()));
                ds.setLogAbandoned(Boolean.parseBoolean(basic.getLogAbandoned()));
            } else {
                ds.setRemoveAbandoned(false);
            }

            // 空闲连接检查配置
            ds.setTimeBetweenEvictionRunsMillis(Integer.parseInt(basic.getTimeBetweenEvictionRunsMillis()));
            ds.setMinEvictableIdleTimeMillis(Integer.parseInt(basic.getMinEvictableIdleTimeMillis()));

            // 连接测试配置
            ds.setValidationQuery(basic.getValidationQuery());
            ds.setValidationQueryTimeout(3);
            ds.setTestWhileIdle(Boolean.parseBoolean(basic.getTestWhileIdle()));

            // 连接测试策略
            boolean testOnBorrow = Boolean.parseBoolean(basic.getTestOnBorrow());
            boolean testOnReturn = Boolean.parseBoolean(basic.getTestOnReturn());

            if (testOnBorrow || testOnReturn) {
                log.warn("{}: testOnBorrow={}, testOnReturn={} may impact performance",
                        dsName, testOnBorrow, testOnReturn);
            }

            ds.setTestOnBorrow(testOnBorrow);
            ds.setTestOnReturn(testOnReturn);

            // 设置过滤器 - 修正版本
            ds.setProxyFilters(Lists.newArrayList(statFilter()));

            // 可以通过filters属性添加更多过滤器
            ds.setFilters("stat,wall,log4j2");

            // 其他性能优化配置
            ds.setPoolPreparedStatements(true);
            ds.setMaxPoolPreparedStatementPerConnectionSize(20);

            // 连接错误重试
            ds.setBreakAfterAcquireFailure(false);
            ds.setConnectionErrorRetryAttempts(3);

            // 事务配置
            ds.setDefaultAutoCommit(true);
            // 使用具体的隔离级别常量
            ds.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            log.debug("{} initialized with config: maxActive={}, minIdle={}, initialSize={}",
                    dsName, ds.getMaxActive(), ds.getMinIdle(), ds.getInitialSize());

        } catch (Exception e) {
            log.error("Failed to initialize datasource {}: {}", dsName, e.getMessage(), e);
            throw new RuntimeException("Datasource initialization failed: " + dsName, e);
        }
    }

    private Filter statFilter() {
        StatFilter filter = new StatFilter();
        filter.setSlowSqlMillis(5000);
        filter.setLogSlowSql(true);
        filter.setMergeSql(true);
        return filter;
    }

    /**
     * 优雅关闭数据源的方法
     */
    public void closeDataSource(DataSource dataSource) {
        if (dataSource instanceof DruidDataSource) {
            try {
                ((DruidDataSource) dataSource).close();
                log.info("Datasource closed successfully");
            } catch (Exception e) {
                log.error("Error closing datasource: {}", e.getMessage());
            }
        }
    }
}