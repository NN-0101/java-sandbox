package com.sandbox.services.living.ai.chat;

import com.sandbox.services.living.ai.annotations.AiChatMessageService;
import com.sandbox.services.living.enumeration.ai.AiTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * 通用闲聊类型的 AI 聊天服务实现
 *
 * <p>该类通过 {@link AiChatMessageService} 注解标记为 {@link AiTypeEnum#TALK} 类型的服务，
 * 在 {@link com.sandbox.services.living.ai.config.AiConfig#chatMessageStrategyMap} 中注册为策略实现。
 *
 * <p><b>功能说明：</b>
 * <ul>
 *   <li>不附加任何工具或特定提示词，仅进行普通对话</li>
 *   <li>支持多轮对话记忆（通过 {@link ChatMemory}）</li>
 *   <li>返回响应式流 {@link Flux} 支持流式输出</li>
 * </ul>
 *
 * @author 0101
 * @see AiTypeEnum#TALK
 * @since 2026/03/18
 */
@Slf4j
@Service
@AiChatMessageService(AiTypeEnum.TALK)
public class TalkChatMessage implements BaseChatMessage {

    @Autowired
    private ChatClient chatClient;

    /**
     * 发送用户消息并返回 AI 回复流，仅进行普通对话
     *
     * <p>不附加任何系统提示词或工具，仅基于对话历史进行响应。
     *
     * @param user           用户标识
     * @param conversationId 会话 ID
     * @param query          用户查询内容
     * @return AI 回复的响应式流
     */
    @Override
    public Flux<String> sendMessage(String user, String conversationId, String query) {
        return chatClient
                .prompt()
                .user(query)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }
}