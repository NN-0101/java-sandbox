package com.sandbox.services.living.service;

import com.sandbox.services.living.security.token.model.AuthorizationTokenBO;

/**
 * @description:
 * @author: 0101
 * @create: 2026/03/15
 */
public interface AuthService {

    /**
     * 密码登录
     *
     * @param phone    手机号
     * @param password 密码
     * @return 令牌信息
     */
    AuthorizationTokenBO passwordLogin(String phone, String password);

    /**
     * 短信验证码登录
     *
     * @param phone   手机号
     * @param captcha 验证码
     * @return 令牌信息
     */
    AuthorizationTokenBO captchaLogin(String phone, String captcha);

    /**
     * 刷新令牌 refreshToken
     *
     * @param refreshToken refreshToken
     * @return 令牌信息
     */
    AuthorizationTokenBO refreshToken(String refreshToken);

    /**
     * 退出登录
     *
     * @param accessToken accessToken
     * @return 受影响行
     */
    int logout(String accessToken);
}
