package com.sandbox.services.living.security.provider;

import com.sandbox.services.living.security.service.CustomUserDetailsService;
import com.sandbox.services.living.security.token.CaptchaAuthenticationToken;
import com.sandbox.services.living.security.user.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * 验证码认证提供者
 *
 * <p>实现基于手机号+验证码的认证方式，是 Spring Security 认证体系中的自定义认证器。
 * 该 Provider 专门处理 {@link CaptchaAuthenticationToken} 类型的认证请求。</p>
 *
 * <p><b>认证流程：</b></p>
 * <ol>
 *   <li><b>接收凭证：</b>从认证令牌中获取手机号（principal）和验证码（credentials）</li>
 *   <li><b>验证码校验：</b>【TODO】调用验证码服务校验手机号与验证码的匹配性</li>
 *   <li><b>用户加载：</b>通过 {@link CustomUserDetailsService} 加载用户信息</li>
 *   <li><b>构建认证结果：</b>创建已认证的 {@link CaptchaAuthenticationToken} 并填充用户权限</li>
 * </ol>
 *
 * <p><b>与其他 Provider 的关系：</b></p>
 * <ul>
 *   <li>{@link CaptchaAuthenticationProvider} - 处理验证码登录（手机号+验证码）</li>
 *   <li>{@link org.springframework.security.authentication.dao.DaoAuthenticationProvider} - 处理密码登录（用户名+密码）</li>
 * </ul>
 *
 * <p><b>异常处理：</b></p>
 * <ul>
 *   <li>用户不存在：抛出 {@link BadCredentialsException} 隐藏用户存在性信息</li>
 *   <li>验证码错误：【TODO】抛出相应的认证异常</li>
 * </ul>
 *
 * @author 0101
 * @see AuthenticationProvider
 * @see CaptchaAuthenticationToken
 * @see CustomUserDetailsService
 * @since 2026-03-14
 */
@Slf4j
@Component
public class CaptchaAuthenticationProvider implements AuthenticationProvider {

    private final CustomUserDetailsService userDetailsService;

    public CaptchaAuthenticationProvider(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        CaptchaAuthenticationToken token = (CaptchaAuthenticationToken) authentication;

        String phone = (String) token.getPrincipal();
        String captcha = (String) token.getCredentials();
        // TODO 验证验证码

        try {
            // 加载用户信息
            CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(phone);

            // 创建已认证的Token
            CaptchaAuthenticationToken authenticatedToken = new CaptchaAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            authenticatedToken.setDetails(token.getDetails());

            return authenticatedToken;

        } catch (UsernameNotFoundException e) {
            throw new BadCredentialsException("用户不存在");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        boolean supported = CaptchaAuthenticationToken.class.isAssignableFrom(authentication);
        log.debug("Checking support for {}: {}", authentication.getName(), supported);
        return supported;
    }
}