package com.sandbox.services.db.mysql.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * @description: 表字段加密配置
 * @author: 0101
 * @create: 2026/3/13
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "aes")
public class TableEncryptConfig {

    private Map<String, List<String>> tables;
}
