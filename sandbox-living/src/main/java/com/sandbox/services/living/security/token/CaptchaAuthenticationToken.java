package com.sandbox.services.living.security.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 验证码认证令牌
 *
 * <p>封装基于手机号和验证码的认证信息，是 {@link org.springframework.security.core.Authentication}
 * 接口的自定义实现。该令牌贯穿整个验证码认证流程，用于在认证各组件间传递认证数据。</p>
 *
 * <p><b>两种状态：</b></p>
 * <ul>
 *   <li><b>未认证状态：</b>使用 {@link #CaptchaAuthenticationToken(Object, Object)} 构造函数创建，
 *       此时仅包含手机号和验证码，权限为空，authenticated = false</li>
 *   <li><b>已认证状态：</b>使用 {@link #CaptchaAuthenticationToken(Object, Object, Collection)}
 *       构造函数创建，包含完整的用户信息和权限，authenticated = true</li>
 * </ul>
 *
 * <p><b>数据存储：</b></p>
 * <ul>
 *   <li>principal：存储手机号（未认证）或 {@link com.sandbox.services.living.security.user.CustomUserDetails}（已认证）</li>
 *   <li>credentials：存储验证码（认证完成后会擦除）</li>
 *   <li>authorities：存储用户权限（仅在已认证状态存在）</li>
 * </ul>
 *
 * <p><b>使用场景：</b></p>
 * <ol>
 *   <li>登录请求到达 {@link com.sandbox.services.living.security.filter.CustomAuthenticationFilter}，
 *       创建未认证令牌并传递给 {@link org.springframework.security.authentication.AuthenticationManager}</li>
 *   <li>{@link com.sandbox.services.living.security.provider.CaptchaAuthenticationProvider}
 *       处理令牌，验证成功后返回已认证令牌</li>
 *   <li>已认证令牌被存入 {@link org.springframework.security.core.context.SecurityContext}</li>
 * </ol>
 *
 * @author 0101
 * @see AbstractAuthenticationToken
 * @see com.sandbox.services.living.security.provider.CaptchaAuthenticationProvider
 * @see com.sandbox.services.living.security.filter.CustomAuthenticationFilter
 * @since 2026-03-14
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
