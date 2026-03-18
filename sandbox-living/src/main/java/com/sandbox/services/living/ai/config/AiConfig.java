package com.sandbox.services.living.ai.config;

import com.sandbox.services.living.ai.annotations.AiChatMessageService;
import com.sandbox.services.living.ai.chat.BaseChatMessage;
import com.sandbox.services.living.ai.custom.CustomChatMemory;
import com.sandbox.services.living.enumeration.ai.AiTypeEnum;
import com.sandbox.services.living.service.ai.ConversationMemoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: xp
 * @create: 2025/5/30
 */
@Slf4j
@Configuration
public class AiConfig {

    @Autowired
    private ConversationMemoryService conversationMemoryService;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {

        return builder
                //设置提示词.defaultSystem()
                .defaultAdvisors(
                        //配置日志
                        new SimpleLoggerAdvisor(),
                        //聊天记忆
                        MessageChatMemoryAdvisor.builder(chatMemory()).build()
                )
                .build();
    }

    /**
     * 聊天记忆
     *
     * @return ChatMemory
     */
    @Bean
    public ChatMemory chatMemory() {
        return new CustomChatMemory(conversationMemoryService);
    }

    /**
     * 全局策略映射Bean  聊天类型
     * 只初始化一次，整个应用生命周期有效
     */
    @Bean
    public Map<AiTypeEnum, BaseChatMessage> chatMessageStrategyMap(List<BaseChatMessage> strategies) {
        return strategies.stream().collect(Collectors.toMap(
                strategy -> {
                    AiChatMessageService annotation = strategy.getClass().getAnnotation(AiChatMessageService.class);
                    return annotation.value();  // 从注解获取业务类型
                },
                Function.identity(),
                (existing, replacement) -> {  // 处理重复键
                    throw new IllegalStateException("发现重复策略: " + existing.getClass().getName()
                            + " 和 " + replacement.getClass().getName());
                }
        ));
    }
}
