package com.sandbox.services.living.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sandbox.services.living.entity.AuthorizationTokenDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description: 授权token表(AuthorizationToken)表数据库访问层
 * @author: 0101
 * @create: 2026-03-15 20:16:17
 */
public interface AuthorizationTokenMapper extends BaseMapper<AuthorizationTokenDO> {

     /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<User> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<AuthorizationTokenDO> entities);
}

