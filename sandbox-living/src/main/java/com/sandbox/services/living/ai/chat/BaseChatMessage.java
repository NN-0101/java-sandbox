package com.sandbox.services.living.ai.chat;

import reactor.core.publisher.Flux;

/**
 * @description:
 * @author: xp
 * @create: 2025/6/1
 */
public interface BaseChatMessage {

    /**
     * 发送对话信息
     *
     * @param user           用户
     * @param conversationId 会话id
     * @param query          询问
     * @return 结果
     */
    Flux<String> sendMessage(String user, String conversationId, String query);
}
