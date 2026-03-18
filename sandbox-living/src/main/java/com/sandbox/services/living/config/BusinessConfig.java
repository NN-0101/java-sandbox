package com.sandbox.services.living.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @description:
 * @author: 0101
 * @create: 2026/3/18
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "business-config")
public class BusinessConfig {

    /**
     * 提示词
     */
    private Map<String, String> prompts;
}
