package com.sandbox.services.living.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.sandbox.services.living.entity.UserConversationDO;

/**
 * @description: 用户AI聊天会话表(UserConversation)表数据库访问层
 * @author: 0101
 * @create: 2026-03-18 14:31:42
 */
public interface UserConversationMapper extends BaseMapper<UserConversationDO> {

     /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<User> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<UserConversationDO> entities);
}

