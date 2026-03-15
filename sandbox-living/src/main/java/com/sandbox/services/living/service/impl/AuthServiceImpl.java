package com.sandbox.services.living.service.impl;

import com.sandbox.services.living.model.bo.token.AuthorizationTokenBO;
import com.sandbox.services.living.security.token.CaptchaAuthenticationToken;
import com.sandbox.services.living.security.user.CustomUserDetails;
import com.sandbox.services.living.service.AuthService;
import com.sandbox.services.living.service.AuthorizationTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: 0101
 * @create: 2026/03/15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;

    private final AuthorizationTokenService authorizationTokenService;

    @Override
    public AuthorizationTokenBO passwordLogin(String phone, String password) {
        // 认证用户
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(phone, password));

        // 获取用户详情
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 创建令牌
        return authorizationTokenService.createToken(userDetails.getUserId(), userDetails.getPhone());
    }

    @Override
    public AuthorizationTokenBO captchaLogin(String phone, String captcha) {
        // TODO 校验验证码
        // 创建验证码认证Token
        CaptchaAuthenticationToken authToken = new CaptchaAuthenticationToken(phone, captcha);

        // 认证（会调用CaptchaAuthenticationProvider）
        Authentication authentication = authenticationManager.authenticate(authToken);

        // 获取用户详情
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        //  创建令牌
        return authorizationTokenService.createToken(userDetails.getUserId(), userDetails.getUsername());
    }

    @Override
    public AuthorizationTokenBO refreshToken(String refreshToken) {
        return null;
    }

    @Override
    public int logout(String accessToken) {
        return authorizationTokenService.remove(accessToken);
    }
}
