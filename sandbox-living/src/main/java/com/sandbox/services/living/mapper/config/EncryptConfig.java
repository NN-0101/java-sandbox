package com.sandbox.services.living.mapper.config;

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
 * 数据库字段加密配置类（基于 ShardingSphere 加密模块）
 *
 * <p>该类负责构建 ShardingSphere 的加密规则配置，实现对指定表中敏感字段的自动加密存储和解密查询。
 * 加密功能对业务代码完全透明，开发者无需手动处理加密解密逻辑。
 *
 * <p><b>核心功能：</b>
 * <ul>
 *   <li>根据 {@link TableEncryptConfig} 中配置的表和字段信息，动态生成加密规则</li>
 *   <li>配置自定义 AES 加密算法，使用配置文件中的 AES 密钥</li>
 *   <li>支持多表、多字段的加密配置</li>
 * </ul>
 *
 * <p><b>加密原理：</b>
 * <ul>
 *   <li>写入数据时：ShardingSphere 拦截 SQL，对指定字段进行加密后存入数据库</li>
 *   <li>查询数据时：ShardingSphere 拦截 SQL，从数据库读取密文后自动解密返回明文</li>
 *   <li>整个过程对应用层透明，应用层操作的是明文数据</li>
 * </ul>
 *
 * <p><b>配置示例（application.yml）：</b>
 * <pre>
 * aes:
 *   key: "your-256-bit-aes-key"  # AES 加密密钥
 *
 * table-encrypt:
 *   tables:
 *     user: ["id_card", "phone", "email"]
 *     order: ["receiver_phone", "receiver_address"]
 * </pre>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>用户敏感信息加密：如身份证号、手机号、邮箱、地址等</li>
 *   <li>支付相关敏感字段加密：如银行卡号、CVN 码等</li>
 *   <li>满足数据安全合规要求（如 GDPR、等保三级）</li>
 * </ul>
 *
 * @author 0101
 * @see TableEncryptConfig
 * @see EncryptRuleConfiguration
 * @see org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration
 * @since 2026-03-13
 */
@Configuration
public class EncryptConfig {

    /**
     * AES 加密密钥，从配置文件中注入
     *
     * <p>密钥长度要求：AES-128 需要 16 位，AES-256 需要 32 位。
     * 实际项目中建议通过密钥管理服务（KMS）获取，避免明文存储在配置文件中。
     */
    @Value("${aes.key}")
    private String aesKey;

    /**
     * 表加密配置，从配置文件中加载
     *
     * <p>配置结构示例：
     * <pre>
     * tables:
     *   user:
     *     - id_card
     *     - phone
     *   order:
     *     - receiver_phone
     * </pre>
     */
    @Resource
    private TableEncryptConfig tableEncryptConfig;

    /**
     * 构建 ShardingSphere 加密规则配置
     *
     * <p><b>处理流程：</b>
     * <ol>
     *   <li>从 {@link TableEncryptConfig} 获取需要加密的表和字段映射</li>
     *   <li>防御性检查：若无加密配置，返回 null（表示不启用加密）</li>
     *   <li>遍历每张表的每个字段，为每个字段创建 {@link EncryptColumnRuleConfiguration}</li>
     *   <li>为每张表创建 {@link EncryptTableRuleConfiguration}，包含该表的所有加密列</li>
     *   <li>创建 AES 加密算法配置 {@link ShardingSphereAlgorithmConfiguration}，并设置密钥</li>
     *   <li>组装所有加密表规则和加密算法，构建 {@link EncryptRuleConfiguration}</li>
     * </ol>
     *
     * <p><b>字段配置说明：</b>
     * <ul>
     *   <li>逻辑列名（logicColumn）：代码中使用的字段名，如 "id_card"</li>
     *   <li>密文列名（cipherColumn）：数据库中存储密文的列名，此处与逻辑列名相同（也可不同）</li>
     *   <li>辅助查询列（assistedQueryColumn）：用于查询的辅助列（如索引列），此处为空字符串</li>
     *   <li>明文列（plainColumn）：存储明文的列（一般不使用），此处为空字符串</li>
     *   <li>加密器名称（encryptorName）：指定使用的加密算法，此处为 "custom_aes"</li>
     * </ul>
     *
     * @return 加密规则配置，若无加密表则返回 null
     */
    public EncryptRuleConfiguration buildEncryptRule() {
        // 获取表加密配置
        Map<String, List<String>> tables = tableEncryptConfig.getTables();

        // 防御性检查：如果配置为 null 或空，返回 null 表示不启用加密
        if (tables == null || tables.isEmpty()) {
            return null;
        }

        // 创建加密表规则列表
        List<EncryptTableRuleConfiguration> encryptTableRuleConfigurations = new ArrayList<>(tables.size());

        // 遍历所有需要加密的表
        for (Map.Entry<String, List<String>> encryptMap : tables.entrySet()) {
            String tableName = encryptMap.getKey();           // 表名，如 "user"
            List<String> encryptColumns = encryptMap.getValue(); // 该表需要加密的字段列表

            // 为每个字段创建加密列配置
            List<EncryptColumnRuleConfiguration> encryptColumnConfigList = new ArrayList<>(encryptColumns.size());
            for (String encryptColumn : encryptColumns) {
                // 创建加密列配置
                EncryptColumnRuleConfiguration encryptColumnConfig = new EncryptColumnRuleConfiguration(
                        encryptColumn,        // 逻辑列名（代码中使用）
                        encryptColumn,        // 密文列名（数据库中存储密文的列）
                        "",                    // 辅助查询列（用于索引查询，可选项）
                        "",                    // 明文列（一般不使用）
                        "custom_aes"           // 加密器名称，与下方定义的加密算法名称对应
                );
                encryptColumnConfigList.add(encryptColumnConfig);
            }

            // 创建加密表规则配置
            EncryptTableRuleConfiguration tableRuleConfiguration = new EncryptTableRuleConfiguration(
                    tableName,                      // 表名
                    encryptColumnConfigList,        // 该表的加密列配置列表
                    true                             // 是否查询时自动解密（true 表示解密）
            );
            encryptTableRuleConfigurations.add(tableRuleConfiguration);
        }

        // 配置加密算法属性
        Properties props = new Properties();
        props.setProperty("aes.key.value", aesKey);  // 设置 AES 密钥

        // 创建加密算法配置
        // 注意：算法类型 "mysql" 是 ShardingSphere 内置的 AES 加密算法实现
        ShardingSphereAlgorithmConfiguration aesAlgorithm = new ShardingSphereAlgorithmConfiguration("mysql", props);

        // 构造并返回加密规则配置
        return new EncryptRuleConfiguration(
                encryptTableRuleConfigurations,                     // 加密表规则列表
                Collections.singletonMap("custom_aes", aesAlgorithm) // 加密器名称与算法配置的映射
        );
    }
}