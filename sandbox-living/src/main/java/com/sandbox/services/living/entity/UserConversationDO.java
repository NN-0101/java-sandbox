package com.sandbox.services.living.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.sandbox.services.db.mysql.model.BaseModel;
import java.io.Serial;

/**
 * @description: 用户AI聊天会话表(UserConversation)表数据库实体
 * @author: 0101
 * @create: 2026-03-18 14:31:42
 */
@Data
@TableName("t_user_conversation")
@EqualsAndHashCode(callSuper = true)
public class UserConversationDO extends BaseModel<UserConversationDO> {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 业务类型
     */
    private String businessType;
     
    /**
     * 用户（手机号）
     */
    private String user;
     
    /**
     * 会话名称
     */
    private String name;
     

}
