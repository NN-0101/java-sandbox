package com.sandbox.services.db.mysql.config.prop;

import lombok.Data;

/**
 * JDBC 数据源配置属性类
 *
 * <p>该类用于封装单个数据源的连接配置信息，是 {@link com.sandbox.services.db.mysql.config.DataSourceConfig}
 * 中各个数据源（如 ds0、ds1、ds0slave0 等）的配置载体。通过该类，可以将配置文件中的
 * 数据源属性映射为 Java 对象，便于在代码中统一管理和使用。
 *
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>配置映射：</b>将 YAML 或 properties 配置文件中的数据源属性映射为对象属性</li>
 *   <li><b>信息封装：</b>封装数据源连接所需的必要信息（URL、用户名、密码、驱动类、连接池类型）</li>
 *   <li><b>数据源创建：</b>为 {@link com.sandbox.services.db.mysql.util.DataSourceUtil#buildDataSource}
 *       提供构建数据源所需的参数</li>
 * </ul>
 *
 * <p><b>属性说明：</b>
 * <ul>
 *   <li><b>jdbcUrl：</b>数据库连接 URL，例如：jdbc:mysql://localhost:3306/dbname?useSSL=false</li>
 *   <li><b>username：</b>数据库用户名</li>
 *   <li><b>password：</b>数据库密码</li>
 *   <li><b>type：</b>数据源类型，即连接池实现类的全限定名，例如：com.alibaba.druid.pool.DruidDataSource</li>
 *   <li><b>driverClassName：</b>JDBC 驱动类名，例如：com.mysql.cj.jdbc.Driver</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>数据源配置加载：</b>在 {@link com.sandbox.services.db.mysql.config.DataSourceConfig} 中，
 *       通过 {@code @ConfigurationProperties} 将配置文件的属性绑定到此类实例</li>
 *   <li><b>数据源创建：</b>在创建具体数据源时，将此对象中的属性传递给数据源构建工具</li>
 *   <li><b>多数据源管理：</b>每个数据源（主库、从库）对应一个独立的 JdbcDsProp 实例</li>
 * </ul>
 *
 * <p><b>配置示例（application.yml）：</b>
 * <pre>
 * sharding:
 *   ds0:
 *     type: com.alibaba.druid.pool.DruidDataSource
 *     driver-class-name: com.mysql.cj.jdbc.Driver
 *     jdbc-url: jdbc:mysql://192.168.1.100:3306/db0?useUnicode=true&characterEncoding=utf8
 *     username: root
 *     password: 123456
 *   ds0slave0:
 *     type: com.alibaba.druid.pool.DruidDataSource
 *     driver-class-name: com.mysql.cj.jdbc.Driver
 *     jdbc-url: jdbc:mysql://192.168.1.101:3306/db0?useUnicode=true&characterEncoding=utf8
 *     username: readonly
 *     password: readonly123
 * </pre>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li><b>简单性：</b>只包含数据源连接必需的基本属性，复杂的连接池配置由 {@link com.sandbox.services.db.mysql.config.prop.JdbcBasicProp} 管理</li>
 *   <li><b>可扩展：</b>如果需要添加更多数据源属性（如连接属性、超时设置），可直接在此类中添加字段</li>
 *   <li><b>类型安全：</b>使用 String 类型存储所有属性，后续根据需要可以增加类型转换</li>
 *   <li><b>与 Spring Boot 集成：</b>属性命名遵循 Spring Boot 的 Relaxed Binding 规则，
 *       支持驼峰、中划线等多种命名方式</li>
 * </ul>
 *
 * @author 0101
 * @see com.sandbox.services.db.mysql.config.DataSourceConfig
 * @see com.sandbox.services.db.mysql.util.DataSourceUtil
 * @see com.sandbox.services.db.mysql.config.prop.JdbcBasicProp
 * @since 2026-03-13
 */
@Data
public class JdbcDsProp {

    /**
     * 数据库连接 URL
     *
     * <p>JDBC 连接字符串，包含数据库地址、端口、数据库名以及连接参数。
     * 例如：jdbc:mysql://localhost:3306/my_db?useSSL=false&serverTimezone=Asia/Shanghai
     */
    private String jdbcUrl;

    /**
     * 数据库用户名
     *
     * <p>连接数据库使用的用户名，应具有相应的访问权限。
     * 建议为不同数据源（主/从）设置不同权限的用户，从库使用只读用户。
     */
    private String username;

    /**
     * 数据库密码
     *
     * <p>连接数据库使用的密码。
     * 生产环境中建议使用加密存储或通过外部配置中心获取。
     */
    private String password;

    /**
     * 数据源类型
     *
     * <p>连接池实现类的全限定类名，例如：
     * <ul>
     *   <li>com.alibaba.druid.pool.DruidDataSource - Druid 连接池</li>
     *   <li>com.zaxxer.hikari.HikariDataSource - HikariCP 连接池</li>
     *   <li>org.apache.tomcat.jdbc.pool.DataSource - Tomcat JDBC Pool</li>
     * </ul>
     */
    private String type;

    /**
     * JDBC 驱动类名
     *
     * <p>数据库驱动的全限定类名，例如：
     * <ul>
     *   <li>com.mysql.cj.jdbc.Driver - MySQL 8.0+ 驱动</li>
     *   <li>com.mysql.jdbc.Driver - MySQL 5.x 驱动</li>
     *   <li>oracle.jdbc.driver.OracleDriver - Oracle 驱动</li>
     * </ul>
     */
    private String driverClassName;
}