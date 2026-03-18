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
 * 该 Provider 专门处理 {@link CaptchaAuthenticationToken} 类型的认证请求，支持无密码的验证码登录场景。
 *
 * <p><b>认证流程：</b>
 * <ol>
 *   <li><b>接收凭证：</b>从认证令牌中获取手机号（principal）和验证码（credentials）</li>
 *   <li><b>验证码校验：</b>【TODO】调用验证码服务校验手机号与验证码的匹配性</li>
 *   <li><b>用户加载：</b>通过 {@link CustomUserDetailsService#loadUserByUsername(String)} 加载用户信息</li>
 *   <li><b>构建认证结果：</b>创建已认证的 {@link CaptchaAuthenticationToken} 并填充用户权限</li>
 *   <li><b>返回认证结果：</b>将认证令牌存入 SecurityContext，完成认证</li>
 * </ol>
 *
 * <p><b>与其他 Provider 的关系：</b>
 * <ul>
 *   <li>{@link CaptchaAuthenticationProvider} - 处理验证码登录（手机号+验证码）</li>
 *   <li>{@link org.springframework.security.authentication.dao.DaoAuthenticationProvider} - 处理密码登录（用户名+密码）</li>
 * </ul>
 * 两者共存于 {@link org.springframework.security.authentication.ProviderManager} 中，
 * 根据认证令牌类型自动选择合适的 Provider 进行处理。
 *
 * <p><b>异常处理策略：</b>
 * <ul>
 *   <li><b>用户不存在：</b>抛出 {@link BadCredentialsException}，避免暴露用户是否存在的信息（防止用户枚举攻击）</li>
 *   <li><b>验证码错误：</b>【TODO】抛出相应的认证异常（如 CaptchaInvalidException）</li>
 *   <li><b>其他异常：</b>统一转换为 AuthenticationException 的子类，由认证入口点处理</li>
 * </ul>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li>实现 {@link AuthenticationProvider} 接口，融入 Spring Security 标准认证体系</li>
 *   <li>通过 {@link #supports(Class)} 方法明确声明支持 {@link CaptchaAuthenticationToken} 类型</li>
 *   <li>认证成功后返回的令牌中 credentials 设置为 null，避免敏感信息在内存中滞留</li>
 *   <li>保持与原有 UserDetailsService 的兼容，复用已有的用户数据加载逻辑</li>
 * </ul>
 *
 * @author 0101
 * @see AuthenticationProvider
 * @see CaptchaAuthenticationToken
 * @see CustomUserDetailsService
 * @see org.springframework.security.authentication.ProviderManager
 * @since 2026-03-14
 */
@Slf4j
@Component
public class CaptchaAuthenticationProvider implements AuthenticationProvider {

    /**
     * 自定义用户详情服务，用于根据手机号加载用户信息
     */
    private final CustomUserDetailsService userDetailsService;

    /**
     * 构造方法注入
     *
     * @param userDetailsService 用户详情服务
     */
    public CaptchaAuthenticationProvider(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * 执行验证码认证
     *
     * <p><b>处理步骤：</b>
     * <ol>
     *   <li><b>类型转换：</b>将 Authentication 转换为 {@link CaptchaAuthenticationToken}</li>
     *   <li><b>提取凭证：</b>获取手机号（principal）和验证码（credentials）</li>
     *   <li><b>验证码校验：</b>【TODO】调用验证码服务进行校验</li>
     *   <li><b>加载用户：</b>通过手机号加载用户信息</li>
     *   <li><b>构建认证结果：</b>创建已认证的令牌，包含用户详情和权限信息</li>
     *   <li><b>设置详情：</b>将原始请求的详情（IP、SessionID 等）复制到新令牌中</li>
     * </ol>
     *
     * @param authentication 认证请求对象，应为 {@link CaptchaAuthenticationToken} 类型
     * @return 已认证的 Authentication 对象
     * @throws AuthenticationException 认证失败时抛出相应异常
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 类型转换
        CaptchaAuthenticationToken token = (CaptchaAuthenticationToken) authentication;

        // 提取手机号和验证码
        String phone = (String) token.getPrincipal();
        String captcha = (String) token.getCredentials();

        log.debug("开始验证码认证 - 手机号: {}", phone);

        // TODO 调用验证码服务校验手机号和验证码
        // 示例：captchaService.validate(phone, captcha);

        try {
            // 加载用户信息
            CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(phone);

            log.debug("用户信息加载成功 - userId: {}, phone: {}, authorities: {}",
                    userDetails.getUserId(), userDetails.getPhone(), userDetails.getAuthorities());

            // 创建已认证的Token
            // 注意：credentials 设置为 null，避免敏感信息在内存中保留
            CaptchaAuthenticationToken authenticatedToken = new CaptchaAuthenticationToken(
                    userDetails,                 // principal：用户详情
                    null,                         // credentials：凭证置空
                    userDetails.getAuthorities()  // authorities：用户权限
            );

            // 复制请求详情（如IP地址）到新令牌
            authenticatedToken.setDetails(token.getDetails());

            log.debug("验证码认证成功 - 手机号: {}", phone);
            return authenticatedToken;

        } catch (UsernameNotFoundException e) {
            // 用户不存在时抛出 BadCredentialsException，隐藏用户存在性信息
            log.warn("验证码认证失败 - 手机号不存在: {}", phone);
            throw new BadCredentialsException("用户不存在");
        }
    }

    /**
     * 判断当前 Provider 是否支持指定的认证类型
     *
     * <p>该方法由 {@link org.springframework.security.authentication.ProviderManager} 调用，
     * 用于在认证流程中查找能够处理特定认证令牌的 Provider。
     *
     * @param authentication 认证类型 Class 对象
     * @return 如果支持 {@link CaptchaAuthenticationToken} 及其子类则返回 true
     */
    @Override
    public boolean supports(Class<?> authentication) {
        boolean supported = CaptchaAuthenticationToken.class.isAssignableFrom(authentication);
        log.debug("检查 Provider 支持的认证类型 - 类型: {}, 是否支持: {}", authentication.getName(), supported);
        return supported;
    }
}