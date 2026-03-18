package com.sandbox.services.living.service.ai;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sandbox.services.living.entity.UserConversationDO;
import com.sandbox.services.living.model.bo.ai.UserConversationBO;

import java.util.List;

/**
 * @description: 用户AI聊天会话表(UserConversation)表服务接口
 * @author: 0101
 * @create: 2026-03-18 14:31:42
 */
public interface UserConversationService extends IService<UserConversationDO> {

    /**
     * 获取用户对话列表
     *
     * @param user 用户
     * @param type 业务
     * @return 结果
     */
    List<UserConversationBO> get(String user, String type);

    /**
     * 修改会话信息
     *
     * @param userConversationBO 参数
     * @return 结果
     */
    int update(UserConversationBO userConversationBO);
}

