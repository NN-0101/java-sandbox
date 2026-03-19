package com.sandbox.services.living.ai.chat;

import com.sandbox.services.living.ai.annotations.AiChatMessageService;
import com.sandbox.services.living.ai.tool.DBTools;
import com.sandbox.services.living.config.BusinessConfig;
import com.sandbox.services.living.ai.enumeration.AiTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * 数据库查询类型的 AI 聊天服务实现
 *
 * <p>该类通过 {@link AiChatMessageService} 注解标记为 {@link AiTypeEnum#DB} 类型的服务，
 * 在 {@link com.sandbox.services.living.ai.config.AiConfig#chatMessageStrategyMap} 中注册为策略实现。
 *
 * <p><b>功能说明：</b>
 * <ul>
 *   <li>从配置中心获取数据库查询专用的提示词（prompt）</li>
 *   <li>使用 {@link DBTools} 提供 SQL 查询能力，AI 可生成 SQL 并执行</li>
 *   <li>支持多轮对话记忆（通过 {@link ChatMemory}）</li>
 *   <li>返回响应式流 {@link Flux} 支持流式输出</li>
 * </ul>
 *
 * @author 0101
 * @see AiTypeEnum#DB
 * @see DBTools
 * @see BusinessConfig
 * @since 2026/03/18
 */
@Slf4j
@Service
@AiChatMessageService(AiTypeEnum.DB)
public class DBChatMessage implements BaseChatMessage {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private BusinessConfig businessConfig;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 发送用户消息并返回 AI 回复流，支持数据库查询
     *
     * <p><b>处理流程：</b>
     * <ol>
     *   <li>从 {@link BusinessConfig} 中获取与数据库查询相关的提示词</li>
     *   <li>构建 ChatClient 请求，附带用户输入、会话记忆和数据库查询工具</li>
     *   <li>返回流式响应内容</li>
     * </ol>
     *
     * @param user           用户标识
     * @param conversationId 会话 ID
     * @param query          用户查询内容
     * @return AI 回复的响应式流
     */
    @Override
    public Flux<String> sendMessage(String user, String conversationId, String query) {
        String prompt = businessConfig.getPrompts().get(AiTypeEnum.DB.getValue());

        return chatClient
                .prompt(prompt)
                .user(query)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .tools(new DBTools(jdbcTemplate))
                .stream()
                .content();
    }
}