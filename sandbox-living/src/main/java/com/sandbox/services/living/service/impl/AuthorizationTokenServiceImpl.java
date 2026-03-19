package com.sandbox.services.living.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sandbox.services.living.entity.AuthorizationTokenDO;
import com.sandbox.services.living.exception.LivingBusinessException;
import com.sandbox.services.living.mapper.custom.AuthorizationTokenRepository;
import com.sandbox.services.living.security.token.model.AccessTokenValueBO;
import com.sandbox.services.living.security.token.model.AuthorizationTokenBO;
import com.sandbox.services.living.security.token.model.RefreshTokenValueBO;
import com.sandbox.services.living.service.AuthorizationTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * @description: 授权token表(AuthorizationToken)表服务实现类
 * @author: 0101
 * @create: 2026-03-15 19:46:01
 */
@Slf4j
@Service("authorizationTokenService")
public class AuthorizationTokenServiceImpl extends ServiceImpl<AuthorizationTokenRepository, AuthorizationTokenDO> implements AuthorizationTokenService {

    // 令牌过期时间（分钟）
    private static final int ACCESS_TOKEN_EXPIRE_MINUTES = 120;

    private static final int REFRESH_TOKEN_EXPIRE_MINUTES = 60 * 24 * 7;

    @Override
    public AuthorizationTokenBO createToken(String userId, String phone) {
        String accessToken = generateTokenValue();
        String refreshToken = generateTokenValue();

        Date now = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, ACCESS_TOKEN_EXPIRE_MINUTES);
        Date expireDate = calendar.getTime();

        AccessTokenValueBO  accessTokenValue = new AccessTokenValueBO();
        accessTokenValue.setUserId(userId);
        accessTokenValue.setPhone(phone);
        accessTokenValue.setExpireTime(expireDate);

        calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, REFRESH_TOKEN_EXPIRE_MINUTES);
        expireDate = calendar.getTime();

        RefreshTokenValueBO refreshTokenValue = new RefreshTokenValueBO();
        refreshTokenValue.setUserId(userId);
        refreshTokenValue.setPhone(phone);
        refreshTokenValue.setExpireTime(expireDate);
        refreshTokenValue.setAccessToken(accessToken);


        AuthorizationTokenBO authorizationTokenBO = new AuthorizationTokenBO();
        authorizationTokenBO.setAccessToken(accessToken);
        authorizationTokenBO.setAccessTokenValue(accessTokenValue);
        authorizationTokenBO.setRefreshToken(refreshToken);
        authorizationTokenBO.setRefreshTokenValue(refreshTokenValue);

        // 保存数据库
        AuthorizationTokenDO authorizationTokenDO = BeanUtil.copyProperties(authorizationTokenBO, AuthorizationTokenDO.class);
        authorizationTokenDO.setRefreshTokenValue(JSONObject.toJSONString(refreshTokenValue));
        authorizationTokenDO.setAccessTokenValue(JSONObject.toJSONString(accessTokenValue));

        getBaseMapper().insert(authorizationTokenDO);

        return authorizationTokenBO;
    }

    @Override
    public AuthorizationTokenBO validateToken(String accessToken) {
        LambdaQueryWrapper<AuthorizationTokenDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AuthorizationTokenDO::getAccessToken, accessToken);
        AuthorizationTokenDO authorizationTokenDO = getBaseMapper().selectOne(lambdaQueryWrapper);
        if (authorizationTokenDO == null) {
            throw new LivingBusinessException(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }
        AuthorizationTokenBO authorizationTokenBO = new  AuthorizationTokenBO();
        authorizationTokenBO.setRefreshTokenValue(JSONObject.parseObject(authorizationTokenDO.getRefreshTokenValue(),RefreshTokenValueBO.class));
        authorizationTokenBO.setAccessTokenValue(JSONObject.parseObject(authorizationTokenDO.getAccessTokenValue(),AccessTokenValueBO.class));
        authorizationTokenBO.setAccessToken(accessToken);
        authorizationTokenBO.setRefreshToken(authorizationTokenDO.getRefreshToken());
        return authorizationTokenBO;
    }

    @Override
    public AuthorizationTokenBO refreshToken(String refreshToken) {
        LambdaQueryWrapper<AuthorizationTokenDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AuthorizationTokenDO::getRefreshToken, refreshToken);
        AuthorizationTokenDO authorizationTokenDO = getBaseMapper().selectOne(lambdaQueryWrapper);

        getBaseMapper().delete(lambdaQueryWrapper);

        // TODO 重新生成 createToken
        return null;
    }

    @Override
    public int remove(String accessToken) {
        return 0;
    }

    /**
     * 生成令牌值
     */
    private String generateTokenValue() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

