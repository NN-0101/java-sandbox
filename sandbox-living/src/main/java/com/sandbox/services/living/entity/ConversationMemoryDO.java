package com.sandbox.services.living.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.sandbox.services.db.mysql.model.BaseModel;
import java.io.Serial;

/**
 * @description: AI聊天会话记忆表(ConversationMemory)表数据库实体
 * @author: 0101
 * @create: 2026-03-18 14:27:11
 */
@Data
@TableName("t_conversation_memory")
@EqualsAndHashCode(callSuper = true)
public class ConversationMemoryDO extends BaseModel<ConversationMemoryDO> {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 会话id
     */
    private String conversationId;
     
    /**
     * 消息类型
     */
    private String messageType;
     
    /**
     * 消息内容
     */
    private String content;
     
    /**
     * 元数据（JSON格式）
     */
    private String metadata;
     

}
