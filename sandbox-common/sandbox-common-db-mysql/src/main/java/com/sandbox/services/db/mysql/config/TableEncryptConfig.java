package com.sandbox.services.db.mysql.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * 表字段加密配置类
 *
 * <p>该类负责从配置文件中加载需要进行数据加密的表和字段信息，
 * 为 ShardingSphere 的加密功能提供配置数据。通过 {@link ConfigurationProperties}
 * 注解，自动将配置文件中以 "aes" 为前缀的配置项绑定到当前类的属性上。
 *
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>配置加载：</b>从 application.yml 或 application.properties 中加载加密配置</li>
 *   <li><b>表-字段映射：</b>维护需要加密的表名与其对应加密字段列表的映射关系</li>
 * </ul>
 *
 * <p><b>配置示例（application.yml）：</b>
 * <pre>
 * aes:
 *   tables:
 *     user:                    # 表名
 *       - id_card              # 需要加密的字段列表
 *       - phone
 *       - email
 *     order:
 *       - receiver_phone
 *       - receiver_address
 *     payment:
 *       - card_number
 *       - cvv
 * </pre>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>配置管理：</b>集中管理所有需要加密的表和字段，便于维护和调整</li>
 *   <li><b>环境适配：</b>不同环境（开发、测试、生产）可以有不同的加密配置</li>
 * </ul>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li><b>松耦合：</b>加密配置与加密实现分离，配置变更无需修改代码</li>
 *   <li><b>可扩展：</b>支持任意数量的表和字段，只需在配置文件中添加即可</li>
 *   <li><b>类型安全：</b>使用 Map 和 List 结构清晰表达配置层级关系</li>
 *   <li><b>自动绑定：</b>利用 Spring Boot 的 ConfigurationProperties 功能，无需手动解析配置文件</li>
 * </ul>
 *
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>配置前缀 "aes" 应与 {@code application.yml} 中的配置前缀保持一致</li>
 *   <li>表名和字段名必须与数据库中的实际名称完全一致（包括大小写）</li>
 *   <li>如果配置的字段在数据库中不存在，ShardingSphere 启动时会报错</li>
 *   <li>加密字段的数据类型需要能够存储加密后的数据（如 VARCHAR 长度要足够）</li>
 *   <li>生产环境中，建议将敏感配置（如密钥）与表字段配置分离，通过不同前缀管理</li>
 * </ul>
 *
 * @author 0101
 * @see ConfigurationProperties
 * @since 2026-03-13
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "aes")
public class TableEncryptConfig {

    /**
     * 表加密配置映射
     *
     * <p>数据结构：Map&lt;表名, List&lt;加密字段名&gt;&gt;
     *
     * <p><b>Key：</b>数据库表名，如 "user"、"order"、"payment" 等<br>
     * <b>Value：</b>该表中需要加密的字段名列表，如 ["id_card", "phone", "email"]
     *
     * <p>配置加载完成后，该 Map 将包含所有需要加密的表及其对应的加密字段，
     * 后续在构建 ShardingSphere 加密规则时，将遍历此 Map 生成加密表规则。
     */
    private Map<String, List<String>> tables;
}