package com.sandbox.services.living.model.bo.ai;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.springframework.ai.chat.messages.Message;

/**
 * @description:
 * @author: xp
 * @create: 2025/6/1
 */
@Data
public class ChatMemoryBO {

    private String conversationId;

    private String messageType;

    private String content;

    private String metadata;

    public ChatMemoryBO(String conversationId, Message message) {
        this.conversationId = conversationId;
        this.messageType = message.getMessageType().getValue();
        this.content = message.getText();
        this.metadata = JSONObject.toJSONString(message.getMetadata());
    }
}
