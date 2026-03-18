package com.sandbox.services.living.ai.memory;

import com.sandbox.services.living.service.ai.ConversationMemoryService;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * 基于数据库的聊天记忆实现
 *
 * <p>该类实现了 Spring AI 的 {@link ChatMemory} 接口，将对话历史持久化到数据库中，
 * 通过 {@link ConversationMemoryService} 进行实际的增删改查操作。
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>在 {@link com.sandbox.services.living.ai.config.AiConfig} 中作为 ChatMemory 的 Bean 提供</li>
 *   <li>支持多轮对话的上下文记忆</li>
 * </ul>
 *
 * @author 0101
 * @see ChatMemory
 * @see ConversationMemoryService
 * @since 2026/03/18
 */
public class DBChatMemory implements ChatMemory {

    private final ConversationMemoryService conversationMemoryService;

    public DBChatMemory(ConversationMemoryService conversationMemoryService) {
        this.conversationMemoryService = conversationMemoryService;
    }

    /**
     * 向指定会话中添加消息
     *
     * @param conversationId 会话 ID
     * @param messages       要添加的消息列表
     */
    @Override
    public void add(@NonNull String conversationId, @NonNull List<Message> messages) {
        conversationMemoryService.add(conversationId, messages);
    }

    /**
     * 获取指定会话的历史消息
     *
     * @param conversationId 会话 ID
     * @return 历史消息列表
     */
    @NonNull
    @Override
    public List<Message> get(@NonNull String conversationId) {
        return conversationMemoryService.get(conversationId);
    }

    /**
     * 清空指定会话的历史消息
     *
     * @param conversationId 会话 ID
     */
    @Override
    public void clear(@NonNull String conversationId) {
        conversationMemoryService.clear(conversationId);
    }
}