package com.sandbox.services.living.config;

import com.sandbox.services.living.ai.enumeration.AiTypeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 业务配置类，用于加载 application.yml 中以 "business-config" 为前缀的配置项
 *
 * <p><b>配置示例：</b>
 * <pre>
 * business-config:
 *   prompts:
 *     DB: "你是一个数据库助手，可以根据用户问题生成SQL查询。"
 *     TALK: "你是一个友好的闲聊助手。"
 * </pre>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>在 {@link com.sandbox.services.living.ai.chat.DBChatMessage} 中获取数据库查询提示词</li>
 *   <li>可扩展其他业务相关的配置项</li>
 * </ul>
 *
 * @author 0101
 * @see ConfigurationProperties
 * @since 2026/03/18
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "business-config")
public class BusinessConfig {

    /**
     * 提示词映射
     *
     * <p>key 为 {@link AiTypeEnum} 的枚举值，
     * value 为对应的系统提示词。
     */
    private Map<String, String> prompts;
}