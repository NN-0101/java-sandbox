package com.sandbox.services.living.security.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * @description: 自定义验证码认证Token
 * @author: 0101
 * @create: 2026/03/14
 */
public class CaptchaAuthenticationToken extends AbstractAuthenticationToken {

    // 手机号
    private final Object principal;
    // 验证码
    private Object credentials;

    /**
     * 未认证的构造函数
     */
    public CaptchaAuthenticationToken(Object principal, Object credentials) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(false);
    }

    /**
     * 已认证的构造函数
     */
    public CaptchaAuthenticationToken(Object principal, Object credentials,
                                      Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }
}
