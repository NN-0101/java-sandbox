package com.sandbox.services.living.mapper.config;

import com.google.common.collect.Lists;
import com.sandbox.services.db.mysql.config.DataSourceConfig;
import com.sandbox.services.db.mysql.enumeration.DataSourceEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * ShardingSphere 核心配置类（5.0.0 版本）
 *
 * <p>该类是数据库分片、读写分离、数据加密的核心配置入口，负责整合多个数据源并配置 ShardingSphere 的各种规则。
 * 通过该配置，应用可以实现对数据库的透明化分库分表、读写分离和字段级加密。
 *
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>多数据源管理：</b>配置主库和从库的数据源映射</li>
 *   <li><b>读写分离：</b>将读请求路由到从库，写请求路由到主库，提升数据库吞吐量</li>
 *   <li><b>数据加密：</b>对敏感字段进行自动加密存储和解密查询（通过 {@link EncryptConfig}）</li>
 *   <li><b>事务管理：</b>配置基于 ShardingSphere 数据源的事务管理器</li>
 *   <li><b>MyBatis 集成：</b>配置 Mapper 扫描和 SqlSessionTemplate</li>
 * </ul>
 *
 * <p><b>数据源结构：</b>
 * <ul>
 *   <li>ds0（主库1） + ds0slave0（从库1）</li>
 *   <li>ds1（主库2） + ds1slave0（从库2）</li>
 * </ul>
 *
 * <p><b>读写分离策略：</b>
 * <ul>
 *   <li>datasource0 组：主库 ds0，从库 ds0slave0，负载均衡算法为轮询（ROUND_ROBIN）</li>
 *   <li>datasource1 组：主库 ds1，从库 ds1slave0，负载均衡算法为轮询（ROUND_ROBIN）</li>
 * </ul>
 *
 * @author 0101
 * @see DataSourceConfig
 * @see EncryptConfig
 * @see DataSourceEnum
 * @see ReadwriteSplittingRuleConfiguration
 * @see EncryptRuleConfiguration
 * @since 2026-03-13
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({DataSourceConfig.class})
@EnableTransactionManagement(proxyTargetClass = true)
@MapperScan(basePackages = {"com.baomidou.mybatisplus.core.mapper", "com.sandbox.services.living.mapper"},
        sqlSessionTemplateRef = "sqlSessionTemplate")
public class ShardingSphereConfig {

    /**
     * 数据源配置，从配置文件加载数据库连接信息
     */
    @Resource
    DataSourceConfig dataSourceConfig;

    /**
     * 加密配置，用于构建字段加密规则
     */
    @Autowired
    private EncryptConfig encryptConfig;

    /**
     * 读写分离规则中负载均衡算法的名称
     */
    private static final String RULE_CONFIG_LOAD_BALANCER_NAME = "load_balancer";

    /**
     * 创建数据源映射
     *
     * <p>该方法将所有需要管理的数据源（主库和从库）注册到一个 Map 中，
     * 供 ShardingSphere 统一管理和路由。
     *
     * <p><b>数据源包含：</b>
     * <ul>
     *   <li>ds0：主库1（写库）</li>
     *   <li>ds1：主库2（写库）</li>
     *   <li>ds0slave0：ds0 的从库（读库）</li>
     *   <li>ds1slave0：ds1 的从库（读库）</li>
     * </ul>
     *
     * @return 数据源名称到 DataSource 实例的映射
     */
    @Bean("dataSourceMap")
    public Map<String, DataSource> dataSourceMap() {
        Map<String, DataSource> dataSourceMap = new LinkedHashMap<>();
        dataSourceMap.put(DataSourceEnum.DS0.getValue(), dataSourceConfig.ds0());
        dataSourceMap.put(DataSourceEnum.DS1.getValue(), dataSourceConfig.ds1());
        dataSourceMap.put(DataSourceEnum.DS0SLAVE0.getValue(), dataSourceConfig.ds0slave0());
        dataSourceMap.put(DataSourceEnum.DS1SLAVE0.getValue(), dataSourceConfig.ds1slave0());
        return dataSourceMap;
    }

    /**
     * 创建 ShardingSphere 数据源
     *
     * <p>该方法整合所有数据源和规则配置，创建最终的 {@link DataSource} 实例，
     * 该实例集成了读写分离和加密功能，对上层应用完全透明。
     *
     * <p><b>处理流程：</b>
     * <ol>
     *   <li>获取所有真实数据源映射</li>
     *   <li>创建规则配置集合</li>
     *   <li>添加读写分离规则配置（{@link #readWriteConfig()}）</li>
     *   <li>添加加密规则配置（如果存在加密表）</li>
     *   <li>设置属性（如 sql-show 开启 SQL 日志）</li>
     *   <li>调用 {@link ShardingSphereDataSourceFactory#createDataSource} 创建数据源</li>
     * </ol>
     *
     * @return 集成了分片、读写分离、加密功能的 ShardingSphere 数据源
     * @throws SQLException 数据源创建异常
     */
    @Bean("dataSource")
    public DataSource dataSource() throws SQLException {
        // 获取真实数据源映射
        Map<String, DataSource> dataSourceMap = dataSourceMap();

        // 创建规则配置集合
        Collection<RuleConfiguration> ruleConfigurations = new ArrayList<>();

        // 添加读写分离规则
        ruleConfigurations.add(readWriteConfig());

        // 添加数据库字段加密规则（如果有配置）
        EncryptRuleConfiguration encryptRuleConfiguration = encryptConfig.buildEncryptRule();
        if (encryptRuleConfiguration != null) {
            ruleConfigurations.add(encryptRuleConfiguration);
        }

        // 设置 ShardingSphere 属性
        Properties p = new Properties();
        p.setProperty("sql-show", Boolean.TRUE.toString()); // 开启 SQL 日志打印，便于调试

        // 创建并返回 ShardingSphere 数据源
        return ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, ruleConfigurations, p);
    }

    /**
     * 配置读写分离规则
     *
     * <p>定义两个读写分离数据源组：
     * <ul>
     *   <li><b>datasource0：</b>主库 ds0，从库 ds0slave0，使用轮询负载均衡</li>
     *   <li><b>datasource1：</b>主库 ds1，从库 ds1slave0，使用轮询负载均衡</li>
     * </ul>
     *
     * <p><b>规则说明：</b>
     * <ul>
     *   <li>写操作（INSERT、UPDATE、DELETE）路由到主库（ds0/ds1）</li>
     *   <li>读操作（SELECT）根据负载均衡策略路由到从库（ds0slave0/ds1slave0）</li>
     *   <li>负载均衡算法为 ROUND_ROBIN（轮询），平均分配读请求</li>
     * </ul>
     *
     * @return 读写分离规则配置
     */
    public ReadwriteSplittingRuleConfiguration readWriteConfig() {
        // 配置第一个读写分离数据源组（ds0 + ds0slave0）
        ReadwriteSplittingDataSourceRuleConfiguration configuration1 = new ReadwriteSplittingDataSourceRuleConfiguration(
                "datasource0",  // 数据源组名称
                "",             // 暂未使用
                "ds0",          // 主数据源名称
                List.of("ds0slave0"), // 从数据源列表
                "ROUND_ROBIN"   // 负载均衡算法
        );

        // 配置第二个读写分离数据源组（ds1 + ds1slave0）
        ReadwriteSplittingDataSourceRuleConfiguration configuration2 = new ReadwriteSplittingDataSourceRuleConfiguration(
                "datasource1",
                "",
                "ds1",
                List.of("ds1slave0"),
                "ROUND_ROBIN"
        );

        // 将所有读写分离数据源组配置放入列表
        ArrayList<ReadwriteSplittingDataSourceRuleConfiguration> readwriteSplittingDataSourceRuleConfigurations =
                Lists.newArrayList(configuration1, configuration2);

        // 配置负载均衡算法
        Map<String, ShardingSphereAlgorithmConfiguration> loadBalancers = new HashMap<>(1);
        loadBalancers.put(RULE_CONFIG_LOAD_BALANCER_NAME,
                new ShardingSphereAlgorithmConfiguration("ROUND_ROBIN", new Properties()));

        // 返回读写分离规则配置
        return new ReadwriteSplittingRuleConfiguration(readwriteSplittingDataSourceRuleConfigurations, loadBalancers);
    }

    /**
     * 配置事务管理器
     *
     * <p>由于使用了 ShardingSphere 数据源，必须手动配置事务管理器，
     * 以确保事务能够正确地在分片数据源上生效。
     *
     * @param dataSource ShardingSphere 数据源
     * @return 数据源事务管理器
     */
    @Bean
    public DataSourceTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * 配置 SqlSessionTemplate
     *
     * <p>SqlSessionTemplate 是 MyBatis-Spring 的核心类，负责管理 MyBatis 的 SqlSession。
     * 使用 {@link Primary} 注解确保在多个 SqlSessionTemplate 时优先使用此实例。
     *
     * @param sqlSessionFactory MyBatis 的 SqlSessionFactory
     * @return SqlSessionTemplate 实例
     */
    @Bean("sqlSessionTemplate")
    @Primary
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}