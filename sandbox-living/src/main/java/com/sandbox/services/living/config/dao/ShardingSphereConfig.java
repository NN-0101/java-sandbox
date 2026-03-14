package com.sandbox.services.living.config.dao;

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
 * @description: ShardingSphereConfig，设置分库分表，读写分离等策略 5.0.0 版本
 * @author: 0101
 * @create: 2026/3/13
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({DataSourceConfig.class})
@EnableTransactionManagement(proxyTargetClass = true)
@MapperScan(basePackages = {"com.baomidou.mybatisplus.core.mapper", "com.sandbox.services.living.mapper"},
        sqlSessionTemplateRef = "sqlSessionTemplate")
public class ShardingSphereConfig {

    @Resource
    DataSourceConfig dataSourceConfig;

    @Autowired
    private EncryptConfig encryptConfig;

    /**
     * 数据库规则配置负载均衡名称
     */
    private static final String RULE_CONFIG_LOAD_BALANCER_NAME = "load_balancer";

    /**
     * dataSourceMap
     *
     * @return 数据源
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
     * 数据源
     *
     * @return DataSource
     * @throws SQLException DataSource
     */
    @Bean("dataSource")
    public DataSource dataSource() throws SQLException {
        //配置真实数据源
        Map<String, DataSource> dataSourceMap = dataSourceMap();

        //配置分片规则
        Collection<RuleConfiguration> ruleConfigurations = new ArrayList<>();
        //配置读写分离
        ruleConfigurations.add(readWriteConfig());
        //配置数据库字段加密
        EncryptRuleConfiguration encryptRuleConfiguration = encryptConfig.buildEncryptRule();
        if (encryptRuleConfiguration != null) {
            ruleConfigurations.add(encryptRuleConfiguration);
        }
        Properties p = new Properties();
        p.setProperty("sql-show", Boolean.TRUE.toString());
        // 创建 ShardingSphereDataSource
        return ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, ruleConfigurations, p);
    }


    /**
     * 配置读写分离
     *
     * @return 读写分离配置
     */
    public ReadwriteSplittingRuleConfiguration readWriteConfig() {
        ReadwriteSplittingDataSourceRuleConfiguration configuration1 = new ReadwriteSplittingDataSourceRuleConfiguration(
                "datasource0", "", "ds0", List.of("ds0slave0"), "ROUND_ROBIN");
        ReadwriteSplittingDataSourceRuleConfiguration configuration2 = new ReadwriteSplittingDataSourceRuleConfiguration(
                "datasource1", "", "ds1", List.of("ds1slave0"), "ROUND_ROBIN");

        ArrayList<ReadwriteSplittingDataSourceRuleConfiguration> readwriteSplittingDataSourceRuleConfigurations =
                Lists.newArrayList(configuration1, configuration2);

        Map<String, ShardingSphereAlgorithmConfiguration> loadBalancers = new HashMap<>(1);
        // 读写分离算法类型:轮询
        loadBalancers.put(RULE_CONFIG_LOAD_BALANCER_NAME, new ShardingSphereAlgorithmConfiguration("ROUND_ROBIN", new Properties()));

        return new ReadwriteSplittingRuleConfiguration(readwriteSplittingDataSourceRuleConfigurations, loadBalancers);
    }


    /**
     * 需要手动配置事务管理器
     *
     * @param dataSource 数据源
     * @return DataSourceTransactionManager
     */
    @Bean
    public DataSourceTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * sqlSessionTemplate
     *
     * @param sqlSessionFactory sqlSessionFactory
     * @return SqlSessionTemplate
     */
    @Bean("sqlSessionTemplate")
    @Primary
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
