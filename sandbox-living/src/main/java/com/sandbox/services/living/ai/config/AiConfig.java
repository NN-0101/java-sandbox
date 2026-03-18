package com.sandbox.services.living.ai.config;

import com.sandbox.services.living.ai.annotations.AiChatMessageService;
import com.sandbox.services.living.ai.chat.BaseChatMessage;
import com.sandbox.services.living.ai.memory.DBChatMemory;
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
 * AI 模块的配置类，负责初始化 ChatClient、ChatMemory 以及 AI 服务策略映射
 *
 * <p><b>主要功能：</b>
 * <ul>
 *   <li>配置 {@link ChatClient}，并附加日志顾问和记忆顾问</li>
 *   <li>提供基于数据库的 {@link ChatMemory} 实现 {@link DBChatMemory}</li>
 *   <li>构建 {@link AiTypeEnum} 到 {@link BaseChatMessage} 的策略映射，供上层调用</li>
 * </ul>
 *
 * @author 0101
 * @see BaseChatMessage
 * @see AiChatMessageService
 * @see DBChatMemory
 * @since 2026/03/18
 */
@Slf4j
@Configuration
public class AiConfig {

    @Autowired
    private ConversationMemoryService conversationMemoryService;

    /**
     * 配置并创建 ChatClient 实例
     *
     * <p>默认附加以下顾问：
     * <ul>
     *   <li>{@link SimpleLoggerAdvisor}：记录请求与响应日志</li>
     *   <li>{@link MessageChatMemoryAdvisor}：基于 {@link ChatMemory} 实现多轮对话记忆</li>
     * </ul>
     *
     * @param builder ChatClient.Builder 构建器
     * @return 配置完成的 ChatClient
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory()).build()
                )
                .build();
    }

    /**
     * 创建基于数据库的聊天记忆实现
     *
     * <p>该实现将对话历史存储到数据库中，通过 {@link ConversationMemoryService} 操作。
     *
     * @return ChatMemory 实现
     */
    @Bean
    public ChatMemory chatMemory() {
        return new DBChatMemory(conversationMemoryService);
    }

    /**
     * 构建 AI 聊天服务策略映射，根据注解 {@link AiChatMessageService} 区分不同类型
     *
     * <p><b>策略构建逻辑：</b>
     * <ul>
     *   <li>从 Spring 容器中获取所有 {@link BaseChatMessage} 类型的 Bean</li>
     *   <li>通过 {@link AiChatMessageService} 注解获取对应的 {@link AiTypeEnum}</li>
     *   <li>将枚举类型与服务实现映射为 Map，供上层根据类型动态调用</li>
     *   <li>若发现重复的枚举值，则抛出 {@link IllegalStateException}</li>
     * </ul>
     *
     * @param strategies 所有 BaseChatMessage 类型的 Spring Bean
     * @return 类型到服务实现的映射
     * @throws IllegalStateException 当同一个枚举值对应多个实现时抛出
     */
    @Bean
    public Map<AiTypeEnum, BaseChatMessage> chatMessageStrategyMap(List<BaseChatMessage> strategies) {
        return strategies.stream().collect(Collectors.toMap(
                strategy -> {
                    AiChatMessageService annotation = strategy.getClass().getAnnotation(AiChatMessageService.class);
                    return annotation.value();
                },
                Function.identity(),
                (existing, replacement) -> {
                    throw new IllegalStateException("发现重复策略: " + existing.getClass().getName()
                            + " 和 " + replacement.getClass().getName());
                }
        ));
    }
}