package com.sandbox.services.db.mysql.config.prop;

import lombok.Data;

/**
 * 数据库连接池基础配置属性类
 *
 * <p>该类封装了 Druid 连接池的核心性能优化参数，是 {@link com.sandbox.services.db.mysql.config.DataSourceConfig}
 * 中所有数据源共享的基础配置。通过该类，可以集中管理连接池的大小、超时、连接测试、泄漏检测等关键参数，
 * 实现对数据库连接池的精细化调优。
 *
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>连接池大小控制：</b>配置最大活跃连接数、最小空闲连接数、初始连接数</li>
 *   <li><b>连接获取超时：</b>配置获取连接时的最大等待时间</li>
 *   <li><b>连接泄漏检测：</b>配置自动回收泄露连接的机制</li>
 *   <li><b>空闲连接管理：</b>配置空闲连接的检查和清理策略</li>
 *   <li><b>连接有效性验证：</b>配置连接测试的 SQL 和测试策略</li>
 * </ul>
 *
 * <p><b>属性说明：</b>
 * <table border="1">
 *   <tr>
 *     <th>属性名</th>
 *     <th>说明</th>
 *     <th>建议值</th>
 *   </tr>
 *   <tr>
 *     <td>maxActive</td>
 *     <td>最大活跃连接数，连接池中最多能同时存在的连接数</td>
 *     <td>20-100（根据业务并发量调整）</td>
 *   </tr>
 *   <tr>
 *     <td>minIdle</td>
 *     <td>最小空闲连接数，连接池中至少保持的空闲连接数</td>
 *     <td>5-10</td>
 *   </tr>
 *   <tr>
 *     <td>initialSize</td>
 *     <td>初始连接数，启动时创建的连接数量</td>
 *     <td>3-5</td>
 *   </tr>
 *   <tr>
 *     <td>logAbandoned</td>
 *     <td>是否记录连接泄漏日志，配合 removeAbandoned 使用</td>
 *     <td>true（开发环境）、false（生产环境慎用）</td>
 *   </tr>
 *   <tr>
 *     <td>removeAbandoned</td>
 *     <td>是否回收泄露的连接（超过 removeAbandonedTimeout 未关闭的连接）</td>
 *     <td>false（生产环境不推荐）</td>
 *   </tr>
 *   <tr>
 *     <td>removeAbandonedTimeout</td>
 *     <td>连接被认定为泄露的超时时间（秒）</td>
 *     <td>300</td>
 *   </tr>
 *   <tr>
 *     <td>maxWait</td>
 *     <td>获取连接时的最大等待时间（毫秒），-1 表示无限等待</td>
 *     <td>60000（60秒）</td>
 *   </tr>
 *   <tr>
 *     <td>timeBetweenEvictionRunsMillis</td>
 *     <td>空闲连接检查间隔（毫秒），DestroyThread 的运行间隔</td>
 *     <td>60000（60秒）</td>
 *   </tr>
 *   <tr>
 *     <td>numTestsPerEvictionRun</td>
 *     <td>每次检查的空闲连接数量，-1 表示检查所有连接</td>
 *     <td>-1</td>
 *   </tr>
 *   <tr>
 *     <td>minEvictableIdleTimeMillis</td>
 *     <td>连接最小空闲时间（毫秒），超过此时间可能被回收</td>
 *     <td>300000（5分钟）</td>
 *   </tr>
 *   <tr>
 *     <td>validationQuery</td>
 *     <td>连接测试 SQL，用于验证连接是否有效</td>
 *     <td>SELECT 1（MySQL）、SELECT 1 FROM DUAL（Oracle）</td>
 *   </tr>
 *   <tr>
 *     <td>testWhileIdle</td>
 *     <td>是否在空闲时测试连接，建议开启</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>testOnBorrow</td>
 *     <td>是否在获取连接时测试，性能影响较大</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>testOnReturn</td>
 *     <td>是否在归还连接时测试，性能影响较大</td>
 *     <td>false</td>
 *   </tr>
 * </table>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>性能调优：</b>根据业务负载和数据库性能，调整这些参数以优化连接池行为</li>
 *   <li><b>环境适配：</b>不同环境（开发、测试、生产）可以使用不同的配置值</li>
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
 *     test-while-idle: true
 *     test-on-borrow: false
 *     test-on-return: false
 *     time-between-eviction-runs-millis: 60000
 *     min-evictable-idle-time-millis: 300000
 *     remove-abandoned: false
 *     log-abandoned: true
 *     remove-abandoned-timeout: 300
 * </pre>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li><b>统一管理：</b>所有数据源共享同一套基础配置，简化配置和维护</li>
 *   <li><b>字符串类型：</b>属性使用 String 类型，便于从配置文件读取，使用时再进行类型转换</li>
 *   <li><b>灵活扩展：</b>可以根据需要添加更多 Druid 支持的配置项</li>
 *   <li><b>性能优先：</b>默认推荐 testWhileIdle=true、testOnBorrow=false 的性能优化配置</li>
 * </ul>
 *
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>removeAbandoned 相关配置在生产环境需谨慎使用，可能导致正常连接被误回收</li>
 *   <li>maxActive 设置过大会导致数据库资源浪费，设置过小可能导致连接等待</li>
 *   <li>validationQuery 必须与数据库类型匹配，否则会导致连接测试失败</li>
 *   <li>testOnBorrow 和 testOnReturn 会显著降低性能，建议仅在需要严格保证连接有效性时开启</li>
 * </ul>
 *
 * @author 0101
 * @see com.sandbox.services.db.mysql.config.DataSourceConfig
 * @see com.alibaba.druid.pool.DruidDataSource
 * @since 2026-03-13
 */
@Data
public class JdbcBasicProp {

    /**
     * 最大活跃连接数
     *
     * <p>连接池中最多能同时存在的活跃连接数。超过此数的请求将等待空闲连接。
     * 设置过大会增加数据库压力，设置过小会导致请求排队。
     */
    private String maxActive;

    /**
     * 最小空闲连接数
     *
     * <p>连接池中至少保持的空闲连接数，用于应对突发流量。
     */
    private String minIdle;

    /**
     * 初始连接数
     *
     * <p>连接池启动时创建的初始连接数量。设置过大会延长启动时间。
     */
    private String initialSize;

    /**
     * 是否记录连接泄漏日志
     *
     * <p>当 removeAbandoned 开启时，记录被回收的连接日志，便于定位问题。
     */
    private String logAbandoned;

    /**
     * 是否回收泄露连接
     *
     * <p>开启后，超过 removeAbandonedTimeout 未关闭的连接会被强制回收。
     * 生产环境建议关闭，避免误回收正常连接。
     */
    private String removeAbandoned;

    /**
     * 连接泄漏超时时间（秒）
     *
     * <p>连接被认定为泄漏的超时阈值，超过此时间未关闭的连接将被回收。
     */
    private String removeAbandonedTimeout;

    /**
     * 获取连接的最大等待时间（毫秒）
     *
     * <p>从连接池获取连接时，如果无可用连接，最多等待的时间。
     * -1 表示无限等待，0 表示不等待。
     */
    private String maxWait;

    /**
     * 空闲连接检查间隔（毫秒）
     *
     * <p>后台线程定期检查空闲连接的时间间隔，用于清理过期连接。
     */
    private String timeBetweenEvictionRunsMillis;

    /**
     * 每次检查的空闲连接数量
     *
     * <p>每次检查时，最多检查多少个空闲连接。-1 表示检查所有连接。
     */
    private String numTestsPerEvictionRun;

    /**
     * 连接最小空闲时间（毫秒）
     *
     * <p>连接在池中空闲超过此时间，可能被回收（取决于 minIdle 设置）。
     */
    private String minEvictableIdleTimeMillis;

    /**
     * 连接测试 SQL
     *
     * <p>用于验证连接是否有效的 SQL 语句，必须简单且快速执行。
     * 例如：MySQL 使用 "SELECT 1"，Oracle 使用 "SELECT 1 FROM DUAL"。
     */
    private String validationQuery;

    /**
     * 是否在空闲时测试连接
     *
     * <p>如果开启，空闲连接会定期使用 validationQuery 进行测试，
     * 无效连接会被丢弃。建议开启。
     */
    private String testWhileIdle;

    /**
     * 是否在获取连接时测试
     *
     * <p>每次获取连接时，都会使用 validationQuery 测试连接有效性。
     * 性能影响较大，建议关闭，由 testWhileIdle 保证连接质量。
     */
    private String testOnBorrow;

    /**
     * 是否在归还连接时测试
     *
     * <p>每次归还连接时，都会使用 validationQuery 测试连接有效性。
     * 性能影响较大，建议关闭。
     */
    private String testOnReturn;
}