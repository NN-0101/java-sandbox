package com.sandbox.services.db.mysql.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 数据源构建工具类
 *
 * <p>该工具类负责根据配置参数动态创建数据源（DataSource）实例。
 * 它通过反射机制加载指定的数据源类型（如 Druid、HikariCP 等），
 * 并使用 Spring Boot 的 {@link DataSourceBuilder} 完成数据源的构建。
 *
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>动态创建：</b>根据传入的配置 Map 动态创建不同类型的数据源</li>
 *   <li><b>类型支持：</b>支持多种数据源类型（Druid、HikariCP、Tomcat JDBC 等）</li>
 *   <li><b>配置驱动：</b>通过配置参数驱动数据源创建，支持运行时动态加载</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>多数据源配置：</b>在 {@link com.sandbox.services.db.mysql.config.DataSourceConfig} 中
 *       通过读取配置动态创建多个数据源</li>
 *   <li><b>运行时新增数据源：</b>可用于支持动态添加新的数据库连接（如多租户场景）</li>
 * </ul>
 *
 * <p><b>配置参数说明：</b>
 * 传入的 {@code dataSourceMap} 应包含以下键值：
 * <ul>
 *   <li><b>type</b>（可选）：数据源类的全限定名，默认值为 "com.alibaba.druid.pool.DruidDataSource"</li>
 *   <li><b>driver</b>：JDBC 驱动类名，如 "com.mysql.cj.jdbc.Driver"</li>
 *   <li><b>url</b>：数据库连接 URL，如 "jdbc:mysql://localhost:3306/dbname"</li>
 *   <li><b>username</b>：数据库用户名</li>
 *   <li><b>password</b>：数据库密码</li>
 * </ul>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li><b>工具类规范：</b>私有构造方法并抛出异常，防止实例化</li>
 *   <li><b>异常处理：</b>捕获 ClassNotFoundException 等异常，记录错误日志并返回 null</li>
 *   <li><b>类型安全：</b>通过泛型和强制类型转换确保返回正确的 DataSource 类型</li>
 *   <li><b>默认值支持：</b>当未指定 type 时，默认使用 Druid 数据源（阿里成熟的数据库连接池）</li>
 * </ul>
 *
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>调用方需要对返回的 null 进行处理，避免空指针异常</li>
 *   <li>数据源构建失败时会返回 null，调用方应检查返回值</li>
 *   <li>使用反射加载类，需要确保相关数据源依赖已添加到项目中</li>
 *   <li>此工具类不会对数据源进行连接测试，调用方可能需要额外验证</li>
 * </ul>
 *
 * @author 0101
 * @see DataSourceBuilder
 * @see com.sandbox.services.db.mysql.config.DataSourceConfig
 * @since 2026-03-13
 */
@Slf4j
public class DataSourceUtil {

    /**
     * 私有构造方法，防止实例化
     *
     * <p>工具类不应被实例化，所有方法均为静态方法。
     * 抛出 {@link IllegalStateException} 以确保即使通过反射也无法实例化。
     */
    private DataSourceUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 构建数据源
     *
     * <p>根据传入的配置参数动态创建数据源实例。
     *
     * <p><b>处理流程：</b>
     * <ol>
     *   <li>从配置 Map 中获取数据源类型（type），如果为空则使用默认的 Druid 数据源</li>
     *   <li>通过反射加载指定的数据源类</li>
     *   <li>从配置 Map 中提取驱动类名（driver）、连接 URL（url）、用户名（username）和密码（password）</li>
     *   <li>使用 {@link DataSourceBuilder} 创建数据源构建器，并设置各项参数</li>
     *   <li>调用 {@link DataSourceBuilder#build()} 方法创建数据源实例</li>
     *   <li>捕获可能发生的异常（如类未找到、配置缺失等），记录错误日志并返回 null</li>
     * </ol>
     *
     * <p><b>示例配置：</b>
     * <pre>
     * Map&lt;String, Object&gt; config = new HashMap&lt;&gt;();
     * config.put("type", "com.zaxxer.hikari.HikariDataSource");
     * config.put("driver", "com.mysql.cj.jdbc.Driver");
     * config.put("url", "jdbc:mysql://localhost:3306/test");
     * config.put("username", "root");
     * config.put("password", "123456");
     *
     * DataSource dataSource = DataSourceUtil.buildDataSource(config);
     * </pre>
     *
     * @param dataSourceMap 数据源配置映射，必须包含 driver、url、username、password 等必要参数
     * @return 构建成功返回 {@link DataSource} 实例，失败返回 null
     */
    public static DataSource buildDataSource(Map<String, Object> dataSourceMap) {
        // 获取数据源类型，如果未指定则默认使用 Druid
        Object type = dataSourceMap.get("type");
        if (type == null) {
            type = "com.alibaba.druid.pool.DruidDataSource";
            log.debug("未指定数据源类型，使用默认 Druid 数据源");
        }

        try {
            // 通过反射加载数据源类
            Class<? extends DataSource> dataSourceType = (Class<? extends DataSource>) Class.forName((String) type);

            // 提取必要的数据库连接参数
            String driverClassName = dataSourceMap.get("driver").toString();
            String url = dataSourceMap.get("url").toString();
            String username = dataSourceMap.get("username").toString();
            String password = dataSourceMap.get("password").toString();

            // 使用 DataSourceBuilder 构建数据源
            DataSourceBuilder<? extends DataSource> factory = DataSourceBuilder.create()
                    .url(url)
                    .username(username)
                    .password(password)
                    .type(dataSourceType)
                    .driverClassName(driverClassName);

            DataSource dataSource = factory.build();
            log.debug("数据源构建成功: type={}, url={}", type, url);
            return dataSource;

        } catch (Exception e) {
            // 捕获所有异常，记录错误日志并返回 null
            log.error("构建数据源出错 - type: {}, 错误信息: {}", type, e.getMessage(), e);
            return null;
        }
    }
}