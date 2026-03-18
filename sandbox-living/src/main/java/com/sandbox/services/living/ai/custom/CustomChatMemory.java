package com.sandbox.services.living.ai.custom;

import com.sandbox.services.living.service.ai.ConversationMemoryService;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.util.List;


/**
 * @description:
 * @author: xp
 * @create: 2025/6/3
 */
public class CustomChatMemory implements ChatMemory {

    private final ConversationMemoryService conversationMemoryService;

    public CustomChatMemory(ConversationMemoryService conversationMemoryService) {
        this.conversationMemoryService = conversationMemoryService;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        conversationMemoryService.add(conversationId, messages);
    }

    @Override
    public List<Message> get(String conversationId) {
        return conversationMemoryService.get(conversationId);
    }

    @Override
    public void clear(String conversationId) {
        conversationMemoryService.clear(conversationId);
    }
}
