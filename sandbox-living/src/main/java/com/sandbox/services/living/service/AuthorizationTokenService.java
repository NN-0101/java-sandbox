package com.sandbox.services.living.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sandbox.services.living.entity.AuthorizationTokenDO;
import com.sandbox.services.living.model.bo.token.AuthorizationTokenBO;

/**
 * @description: 授权token表(AuthorizationToken)表服务接口
 * @author: 0101
 * @create: 2026-03-15 19:46:01
 */
public interface AuthorizationTokenService extends IService<AuthorizationTokenDO> {

    /**
     * 创建令牌
     *
     * @param userId 用户id
     * @param phone  用户手机号
     * @return 令牌信息
     */
    AuthorizationTokenBO createToken(String userId, String phone);

    /**
     * 校验令牌
     *
     * @param accessToken accessToken
     * @return 令牌信息
     */
    AuthorizationTokenBO validateToken(String accessToken);

    /**
     * 刷新令牌 refreshToken
     *
     * @param refreshToken refreshToken
     * @return 令牌信息
     */
    AuthorizationTokenBO refreshToken(String refreshToken);

    /**
     * 删除令牌
     *
     * @param accessToken accessToken
     * @return 受影响行
     */
    int remove(String accessToken);
}

