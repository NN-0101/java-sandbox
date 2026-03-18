package com.sandbox.services.living.service.ai;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sandbox.services.living.entity.ConversationMemoryDO;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * @description: AI聊天会话记忆表(ConversationMemory)表服务接口
 * @author: 0101
 * @create: 2026-03-18 14:27:11
 */
public interface ConversationMemoryService extends IService<ConversationMemoryDO> {

    /**
     * 添加会话记录
     *
     * @param conversationId 会话id
     * @param messages       消息呢荣
     */
    void add(String conversationId, List<Message> messages);

    /**
     * 获取会话记录
     *
     * @param conversationId 会话id
     * @return 结果
     */
    List<Message> get(String conversationId);

    /**
     * 删除会话
     *
     * @param conversationId 会话id
     */
    void clear(String conversationId);
}

