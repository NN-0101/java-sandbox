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
 * @description:
 * @author: xp
 * @create: 2025/6/1
 */
@Slf4j
@Service
@AiChatMessageService(AiTypeEnum.TALK)
public class TalkChatMessage implements BaseChatMessage {

    @Autowired
    private ChatClient chatClient;

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
