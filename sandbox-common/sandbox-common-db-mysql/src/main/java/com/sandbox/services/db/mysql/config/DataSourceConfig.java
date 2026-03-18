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
 * 数据源配置类（优化版）
 *
 * <p>该类是数据库连接的核心配置类，负责创建和配置多个 Druid 数据源实例，
 * 包括主库和从库。通过 {@link ConfigurationProperties} 注解从配置文件中
 * 加载数据源连接信息，并使用 {@link DataSourceUtil} 工具类动态创建数据源。
 *
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>多数据源创建：</b>为 ds0、ds1、ds0slave0、ds1slave0 四个数据源创建对应的 Druid 实例</li>
 *   <li><b>连接池优化：</b>根据业务场景优化 Druid 连接池参数，提升性能和稳定性</li>
 *   <li><b>监控集成：</b>配置 Druid 的 StatFilter，支持慢 SQL 监控和统计</li>
 *   <li><b>环境适配：</b>根据不同运行环境（dev/test/prod）调整连接池配置</li>
 * </ul>
 *
 * <p><b>数据源结构：</b>
 * <ul>
 *   <li><b>ds0：</b>主库1（写库）</li>
 *   <li><b>ds1：</b>主库2（写库）</li>
 *   <li><b>ds0slave0：</b>ds0 的从库（读库）</li>
 *   <li><b>ds1slave0：</b>ds1 的从库（读库）</li>
 * </ul>
 *
 * <p><b>配置参数说明：</b>
 * 配置前缀为 "sharding"，包含以下部分：
 * <ul>
 *   <li><b>ds0 / ds1 / ds0slave0 / ds1slave0：</b>每个数据源的具体连接信息（URL、用户名、密码等）</li>
 *   <li><b>basic：</b>所有数据源共享的基础连接池配置（最大连接数、最小空闲数、超时时间等）</li>
 * </ul>
 *
 * <p><b>配置示例（application.yml）：</b>
 * <pre>
 * sharding:
 *   basic:
 *     max-active: 100
 *     min-idle: 10
 *     initial-size: 5
 *     max-wait: 60000
 *     validation-query: SELECT 1
 *     # ... 其他配置
 *   ds0:
 *     type: com.alibaba.druid.pool.DruidDataSource
 *     driver-class-name: com.mysql.cj.jdbc.Driver
 *     jdbc-url: jdbc:mysql://localhost:3306/db0
 *     username: root
 *     password: 123456
 *   ds0slave0:
 *     # ... 从库配置
 * </pre>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li><b>配置集中管理：</b>所有数据源配置集中在一个类中，便于维护</li>
 *   <li><b>性能优化：</b>针对不同环境优化连接池参数（如开发环境降低初始连接数）</li>
 *   <li><b>监控能力：</b>集成 Druid 的监控功能，支持慢 SQL 日志和统计</li>
 *   <li><b>异常处理：</b>数据源创建失败时抛出运行时异常，避免应用启动成功但数据库不可用</li>
 *   <li><b>资源释放：</b>提供 {@link #closeDataSource(DataSource)} 方法优雅关闭数据源</li>
 * </ul>
 *
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>生产环境建议关闭 removeAbandoned 功能（可能导致连接被误回收）</li>
 *   <li>testOnBorrow 和 testOnReturn 会降低性能，建议仅在需要时开启</li>
 *   <li>不同环境（dev/test/prod）应使用不同的连接池配置</li>
 *   <li>数据源创建失败会导致应用启动失败，确保配置正确</li>
 * </ul>
 *
 * @author 0101
 * @see DruidDataSource
 * @see DataSourceUtil
 * @since 2026-03-13
 */
@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "sharding")
public class DataSourceConfig {

    /**
     * 数据源0（主库1）配置
     */
    private JdbcDsProp ds0;

    /**
     * 数据源1（主库2）配置
     */
    private JdbcDsProp ds1;

    /**
     * 数据源0的从库配置
     */
    private JdbcDsProp ds0slave0;

    /**
     * 数据源1的从库配置
     */
    private JdbcDsProp ds1slave0;

    /**
     * 基础连接池配置（所有数据源共享）
     */
    private JdbcBasicProp basic;

    /**
     * 初始化方法
     *
     * <p>在属性注入完成后执行，记录配置加载日志。
     */
    @PostConstruct
    public void init() {
        log.info("DataSourceConfig initialized with basic properties: {}", basic);
    }

    /**
     * 创建数据源0（主库1）
     *
     * @return 配置好的数据源实例
     */
    public DataSource ds0() {
        return createDatasource(getDs0(), "ds0");
    }

    /**
     * 创建数据源1（主库2）
     *
     * @return 配置好的数据源实例
     */
    public DataSource ds1() {
        return createDatasource(getDs1(), "ds1");
    }

    /**
     * 创建数据源0的从库
     *
     * @return 配置好的数据源实例
     */
    public DataSource ds0slave0() {
        return createDatasource(getDs0slave0(), "ds0slave0");
    }

    /**
     * 创建数据源1的从库
     *
     * @return 配置好的数据源实例
     */
    public DataSource ds1slave0() {
        return createDatasource(getDs1slave0(), "ds1slave0");
    }

    /**
     * 创建并配置 Druid 数据源
     *
     * <p><b>处理流程：</b>
     * <ol>
     *   <li>将 JdbcDsProp 配置转换为 Map，供 {@link DataSourceUtil} 使用</li>
     *   <li>调用 {@link DataSourceUtil#buildDataSource(Map)} 创建基础数据源</li>
     *   <li>设置数据源名称，便于监控识别</li>
     *   <li>调用 {@link #initBasicProperties(DruidDataSource, String)} 配置 Druid 特有属性</li>
     *   <li>返回配置完成的数据源</li>
     * </ol>
     *
     * @param jdbcdsProp 数据源连接配置
     * @param dsName     数据源名称，用于日志和监控
     * @return 配置完成的 Druid 数据源
     * @throws RuntimeException 数据源创建失败时抛出
     */
    private DataSource createDatasource(JdbcDsProp jdbcdsProp, String dsName) {
        long startTime = System.currentTimeMillis();

        // 构建数据源配置 Map
        Map<String, Object> dsMap = new HashMap<>();
        dsMap.put("type", jdbcdsProp.getType());
        dsMap.put("url", jdbcdsProp.getJdbcUrl());
        dsMap.put("driver", jdbcdsProp.getDriverClassName());
        dsMap.put("username", jdbcdsProp.getUsername());
        dsMap.put("password", jdbcdsProp.getPassword());

        // 使用工具类创建基础数据源
        DruidDataSource ds = (DruidDataSource) DataSourceUtil.buildDataSource(dsMap);
        if (ds == null) {
            log.error("创建数据源失败: {}", dsName);
            throw new RuntimeException("创建数据源失败: " + dsName);
        }

        // 设置数据源名称，方便监控识别
        ds.setName(dsName);

        // 初始化 Druid 特有属性
        initBasicProperties(ds, dsName);

        long cost = System.currentTimeMillis() - startTime;
        log.info("数据源 {} 创建成功，耗时: {}ms", dsName, cost);

        return ds;
    }

    /**
     * 初始化 Druid 数据源的基础属性
     *
     * <p>根据基础配置 {@link JdbcBasicProp} 设置 Druid 连接池的各项参数，
     * 并进行环境适配和性能优化。
     *
     * <p><b>配置项说明：</b>
     * <ul>
     *   <li><b>连接池大小：</b>maxActive（最大活跃连接数）、minIdle（最小空闲数）、initialSize（初始连接数）</li>
     *   <li><b>等待超时：</b>maxWait（获取连接的最大等待时间）</li>
     *   <li><b>连接泄漏检测：</b>removeAbandoned（是否回收泄露连接）</li>
     *   <li><b>空闲连接管理：</b>timeBetweenEvictionRunsMillis（空闲连接检查间隔）、minEvictableIdleTimeMillis（最小空闲时间）</li>
     *   <li><b>连接测试：</b>validationQuery（测试SQL）、testWhileIdle（空闲时测试）、testOnBorrow/Return（借/还时测试）</li>
     *   <li><b>性能优化：</b>poolPreparedStatements（开启PS缓存）、maxPoolPreparedStatementPerConnectionSize（PS缓存大小）</li>
     *   <li><b>监控集成：</b>设置 StatFilter 监控慢 SQL</li>
     * </ul>
     *
     * @param ds     待配置的 Druid 数据源
     * @param dsName 数据源名称，用于日志
     */
    private void initBasicProperties(DruidDataSource ds, String dsName) {
        try {
            // ========== 基础连接池配置 ==========
            ds.setMaxActive(Integer.parseInt(basic.getMaxActive()));
            ds.setMinIdle(Integer.parseInt(basic.getMinIdle()));

            // ========== 关键优化：降低初始连接数，加快启动速度 ==========
            int initialSize = Integer.parseInt(basic.getInitialSize());
            String env = System.getProperty("spring.profiles.active", "dev");
            if ("dev".equals(env) || "test".equals(env)) {
                // 开发/测试环境减少初始连接数，加快应用启动
                initialSize = Math.min(initialSize, 1);
            }
            ds.setInitialSize(initialSize);

            // ========== 连接等待配置 ==========
            ds.setMaxWait(Integer.parseInt(basic.getMaxWait()));

            // ========== 连接泄漏检测配置 ==========
            boolean removeAbandoned = Boolean.parseBoolean(basic.getRemoveAbandoned());
            if (removeAbandoned) {
                log.warn("{}: removeAbandoned 已开启，生产环境不推荐使用此配置", dsName);
                ds.setRemoveAbandonedTimeout(Integer.parseInt(basic.getRemoveAbandonedTimeout()));
                ds.setLogAbandoned(Boolean.parseBoolean(basic.getLogAbandoned()));
            } else {
                ds.setRemoveAbandoned(false);
            }

            // ========== 空闲连接检查配置 ==========
            ds.setTimeBetweenEvictionRunsMillis(Integer.parseInt(basic.getTimeBetweenEvictionRunsMillis()));
            ds.setMinEvictableIdleTimeMillis(Integer.parseInt(basic.getMinEvictableIdleTimeMillis()));

            // ========== 连接测试配置 ==========
            ds.setValidationQuery(basic.getValidationQuery());
            ds.setValidationQueryTimeout(3);
            ds.setTestWhileIdle(Boolean.parseBoolean(basic.getTestWhileIdle()));

            // ========== 连接测试策略（性能影响较大） ==========
            boolean testOnBorrow = Boolean.parseBoolean(basic.getTestOnBorrow());
            boolean testOnReturn = Boolean.parseBoolean(basic.getTestOnReturn());

            if (testOnBorrow || testOnReturn) {
                log.warn("{}: testOnBorrow={}, testOnReturn={} 可能影响性能，建议仅在必要时开启",
                        dsName, testOnBorrow, testOnReturn);
            }

            ds.setTestOnBorrow(testOnBorrow);
            ds.setTestOnReturn(testOnReturn);

            // ========== 监控过滤器配置 ==========
            // 设置自定义的 StatFilter 用于慢 SQL 监控
            ds.setProxyFilters(Lists.newArrayList(statFilter()));

            // 通过 filters 属性添加更多过滤器（stat, wall, log4j2）
            ds.setFilters("stat,wall,log4j2");

            // ========== 性能优化配置 ==========
            ds.setPoolPreparedStatements(true);                 // 开启 PreparedStatement 缓存
            ds.setMaxPoolPreparedStatementPerConnectionSize(20); // 每个连接缓存的 PS 数量

            // ========== 连接错误重试配置 ==========
            ds.setBreakAfterAcquireFailure(false);              // 获取连接失败后不中断
            ds.setConnectionErrorRetryAttempts(3);              // 连接错误重试次数

            // ========== 事务配置 ==========
            ds.setDefaultAutoCommit(true);                      // 默认自动提交
            ds.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED); // 事务隔离级别：读已提交

            log.debug("{} 初始化完成 - 最大连接数: {}, 最小空闲: {}, 初始连接数: {}",
                    dsName, ds.getMaxActive(), ds.getMinIdle(), ds.getInitialSize());

        } catch (Exception e) {
            log.error("初始化数据源 {} 失败: {}", dsName, e.getMessage(), e);
            throw new RuntimeException("数据源初始化失败: " + dsName, e);
        }
    }

    /**
     * 创建 Druid 监控过滤器
     *
     * <p>配置 StatFilter 用于统计和监控 SQL 执行情况，包括：
     * <ul>
     *   <li>慢 SQL 监控：执行时间超过 5000ms 的 SQL 会被记录</li>
     *   <li>SQL 合并：将相似 SQL 合并统计</li>
     * </ul>
     *
     * @return 配置好的 StatFilter 实例
     */
    private Filter statFilter() {
        StatFilter filter = new StatFilter();
        filter.setSlowSqlMillis(5000);      // 慢 SQL 阈值（毫秒）
        filter.setLogSlowSql(true);          // 记录慢 SQL 日志
        filter.setMergeSql(true);             // 合并相似 SQL
        return filter;
    }

    /**
     * 优雅关闭数据源
     *
     * <p>在应用关闭或数据源不再使用时调用，确保连接池资源被正确释放。
     *
     * @param dataSource 待关闭的数据源
     */
    public void closeDataSource(DataSource dataSource) {
        if (dataSource instanceof DruidDataSource) {
            try {
                ((DruidDataSource) dataSource).close();
                log.info("数据源关闭成功");
            } catch (Exception e) {
                log.error("关闭数据源时发生错误: {}", e.getMessage());
            }
        }
    }
}