package com.sandbox.services.living.config.dao;

import com.sandbox.services.db.mysql.config.TableEncryptConfig;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @description: 数据库加密配置
 * @author: 0101
 * @create: 2026/3/13
 */
@Configuration
public class EncryptConfig {

    @Value("${aes.key}")
    private String aesKey;
    @Resource
    private TableEncryptConfig tableEncryptConfig;

    public EncryptRuleConfiguration buildEncryptRule() {
        // 创建加密配置
        Map<String, List<String>> tables = tableEncryptConfig.getTables();
        // 防御性检查：如果为null，返回空规则配置
        if (tables == null || tables.isEmpty()) {
            return null;
        }

        List<EncryptTableRuleConfiguration> encryptTableRuleConfigurations = new ArrayList<>(tables.size());

        for (Map.Entry<String, List<String>> encryptMap : tables.entrySet()) {
            String tableName = encryptMap.getKey();
            List<String> encryptColumns = encryptMap.getValue();

            List<EncryptColumnRuleConfiguration> encryptColumnConfigList = new ArrayList<>(encryptColumns.size());
            for (String encryptColumn : encryptColumns) {
                // 1. 加密列配置
                EncryptColumnRuleConfiguration encryptColumnConfig = new EncryptColumnRuleConfiguration(
                        encryptColumn,
                        encryptColumn,
                        "","",
                        "custom_aes"

                );
                encryptColumnConfigList.add(encryptColumnConfig);

            }
            // 2. 加密表配置
            EncryptTableRuleConfiguration tableRuleConfiguration = new EncryptTableRuleConfiguration(
                    tableName,
                    encryptColumnConfigList,
                    true
            );
            encryptTableRuleConfigurations.add(tableRuleConfiguration);
        }

        Properties props = new Properties();
        props.setProperty("aes.key.value", aesKey);
        // 3. 加密算法配置
        ShardingSphereAlgorithmConfiguration aesAlgorithm = new ShardingSphereAlgorithmConfiguration("mysql", props);

        // 4. 构造加密规则
        return new EncryptRuleConfiguration(
                encryptTableRuleConfigurations, // 加密表列表
                Collections.singletonMap("custom_aes", aesAlgorithm) // 加密器映射
        );
    }
}
