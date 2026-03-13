package com.sandbox.services.living.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.sandbox.services.living.model.UserDO;

/**
 * @description: 用户表(User)表数据库访问层
 * @author: 0101
 * @create: 2026-03-13 22:04:03
 */
public interface UserMapper extends BaseMapper<UserDO> {

     /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<User> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<UserDO> entities);
}

