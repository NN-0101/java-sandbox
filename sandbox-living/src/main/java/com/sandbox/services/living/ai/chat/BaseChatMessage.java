package com.sandbox.services.living.ai.chat;

import reactor.core.publisher.Flux;

/**
 * AI 聊天消息处理接口，定义所有 AI 聊天服务必须实现的消息发送方法
 *
 * <p>该接口采用响应式编程模型，返回 {@link Flux} 类型，支持流式输出。
 * 所有具体的 AI 聊天实现类（如 {@link DBChatMessage}、{@link TalkChatMessage}）都应实现该接口。
 *
 * <p><b>核心方法：</b>
 * <ul>
 *   <li>{@link #sendMessage(String, String, String)} 用于发送用户消息并获取 AI 回复流</li>
 * </ul>
 *
 * @author 0101
 * @see DBChatMessage
 * @see TalkChatMessage
 * @since 2026/03/18
 */
public interface BaseChatMessage {

    /**
     * 发送用户消息并返回 AI 回复的响应式流
     *
     * <p>该方法会根据不同的 AI 类型（如数据库查询、闲聊等）执行不同的处理逻辑，
     * 例如 {@link DBChatMessage} 会结合 SQL 工具进行数据库查询，
     * 而 {@link TalkChatMessage} 则仅进行普通对话。
     *
     * @param user           当前发送消息的用户标识
     * @param conversationId 会话 ID，用于维持多轮对话的上下文
     * @param query          用户输入的查询内容
     * @return 返回 AI 回复的响应式流，支持实时推送
     */
    Flux<String> sendMessage(String user, String conversationId, String query);
}