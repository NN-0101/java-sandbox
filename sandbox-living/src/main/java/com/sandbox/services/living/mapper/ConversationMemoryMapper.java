package com.sandbox.services.living.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.sandbox.services.living.entity.ConversationMemoryDO;

/**
 * @description: AI聊天会话记忆表(ConversationMemory)表数据库访问层
 * @author: 0101
 * @create: 2026-03-18 14:27:11
 */
public interface ConversationMemoryMapper extends BaseMapper<ConversationMemoryDO> {

     /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<User> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<ConversationMemoryDO> entities);
}

