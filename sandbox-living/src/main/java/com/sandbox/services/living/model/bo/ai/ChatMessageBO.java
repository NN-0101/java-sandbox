package com.sandbox.services.living.model.bo.ai;

import lombok.Data;

/**
 * @description:
 * @author: xp
 * @create: 2025/6/1
 */
@Data
public class ChatMessageBO {

    private String conversationId;

    private String user;

    private String query;

    private String business;
}
