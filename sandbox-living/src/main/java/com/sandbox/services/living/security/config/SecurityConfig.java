package com.sandbox.services.living.security.config;

import com.sandbox.services.living.security.filter.CustomAuthenticationFilter;
import com.sandbox.services.living.security.handler.CustomAccessDeniedHandler;
import com.sandbox.services.living.security.handler.CustomAuthenticationEntryPoint;
import com.sandbox.services.living.security.provider.CaptchaAuthenticationProvider;
import com.sandbox.services.living.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;

/**
 * Spring Security 核心安全配置类
 *
 * <p>该类是整个安全模块的配置入口，负责定义所有安全相关的规则、过滤器、认证管理器等。
 * 采用无状态（STATELESS）的 JWT 认证方式，不依赖 HttpSession。
 *
 * <p><b>主要配置项：</b>
 * <ul>
 *   <li>禁用 CSRF（适用于无状态 API）</li>
 *   <li>设置会话为无状态（STATELESS）</li>
 *   <li>配置异常处理（认证失败、权限不足）</li>
 *   <li>定义请求授权规则（哪些接口需要认证）</li>
 *   <li>注册自定义认证过滤器 {@link CustomAuthenticationFilter}</li>
 *   <li>配置多个认证提供者（验证码认证、用户名密码认证）</li>
 * </ul>
 *
 * <p><b>认证流程：</b>
 * <ol>
 *   <li>请求到达 {@link CustomAuthenticationFilter}，解析 JWT 并封装认证信息</li>
 *   <li>认证请求由 {@link AuthenticationManager} 分发到合适的 {@link AuthenticationProvider}</li>
 *   <li>支持多种认证方式：验证码登录、用户名密码登录</li>
 *   <li>认证成功后，将认证信息存入 SecurityContext</li>
 * </ol>
 *
 * @author 0101
 * @see CustomAuthenticationFilter
 * @see CustomAuthenticationEntryPoint
 * @see CustomAccessDeniedHandler
 * @see CaptchaAuthenticationProvider
 * @see CustomUserDetailsService
 * @since 2026-03-14
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationFilter customAuthenticationFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CaptchaAuthenticationProvider captchaAuthenticationProvider;

    /**
     * 核心安全过滤器链配置
     *
     * <p>该方法构建完整的 Spring Security 过滤器链，定义所有安全规则。
     *
     * <p><b>配置要点：</b>
     * <ul>
     *   <li><b>CSRF 保护：</b>禁用（使用 JWT 无状态认证，无需 CSRF）</li>
     *   <li><b>会话管理：</b>设置为无状态（STATELESS），不使用 HttpSession 存储安全上下文</li>
     *   <li><b>异常处理：</b>配置自定义的 {@link CustomAuthenticationEntryPoint}（认证失败）和
     *       {@link CustomAccessDeniedHandler}（权限不足）</li>
     *   <li><b>请求授权：</b>配置白名单接口（如登录、验证码、Swagger）允许匿名访问，其他接口需要认证</li>
     *   <li><b>过滤器链：</b>在 {@link UsernamePasswordAuthenticationFilter} 之前添加自定义认证过滤器</li>
     *   <li><b>认证提供者：</b>注册验证码认证和用户名密码认证两个 Provider</li>
     * </ul>
     *
     * @param http HttpSecurity 对象，用于构建过滤器链
     * @return 配置完成的 SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF（适用于无状态 API）
                .csrf(AbstractHttpConfigurer::disable)

                // 设置会话为无状态（不使用 HttpSession）
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 配置异常处理
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)  // 认证失败处理器
                        .accessDeniedHandler(accessDeniedHandler)            // 权限不足处理器
                )

                // 配置请求授权规则
                .authorizeHttpRequests(auth -> auth
                        // 白名单接口：登录、验证码、刷新令牌
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/captcha/send").permitAll()
                        .requestMatchers("/api/auth/captcha/login").permitAll()
                        .requestMatchers("/api/auth/refresh").permitAll()

                        // 放行 Swagger 等文档接口
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // 测试接口需要认证
                        .requestMatchers("/test/**").authenticated()

                        // 其他所有请求都需要认证
                        .anyRequest().authenticated()
                )

                // 添加自定义认证过滤器（在 UsernamePasswordAuthenticationFilter 之前执行）
                .addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 注册认证提供者
        // 注意：这里使用 authenticationProvider 方法逐个注册，而不是通过 AuthenticationManager
        http.authenticationProvider(captchaAuthenticationProvider);
        http.authenticationProvider(daoAuthenticationProvider());

        return http.build();
    }

    /**
     * 用户名密码认证提供者
     *
     * <p>基于 {@link DaoAuthenticationProvider} 实现，处理传统的用户名+密码认证请求。
     *
     * <p><b>实现原理：</b>
     * <ul>
     *   <li>使用 {@link DaoAuthenticationProvider}，它是 Spring Security 默认的认证提供者</li>
     *   <li>设置 {@link CustomUserDetailsService}，用于根据用户名加载用户信息（包括密码、权限等）</li>
     *   <li>设置 {@link PasswordEncoder}，用于验证用户输入的密码与数据库中加密存储的密码是否匹配</li>
     * </ul>
     *
     * <p><b>认证流程：</b>
     * <ol>
     *   <li>接收到用户名+密码的认证请求</li>
     *   <li>调用 {@link CustomUserDetailsService#loadUserByUsername(String)} 加载用户信息</li>
     *   <li>使用 {@link PasswordEncoder#matches(CharSequence, String)} 比对密码</li>
     *   <li>验证通过后返回完整的认证信息（包括权限）</li>
     * </ol>
     *
     * @return AuthenticationProvider 实例
     * @see DaoAuthenticationProvider
     * @see CustomUserDetailsService
     * @see PasswordEncoder
     */
    @Bean
    public AuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * 认证管理器
     *
     * <p>Spring Security 认证流程的核心入口，负责协调多个 {@link AuthenticationProvider}。
     *
     * <p><b>实现原理：</b>
     * <ul>
     *   <li>创建 {@link ProviderManager}，它是 {@link AuthenticationManager} 的默认实现</li>
     *   <li>将多个认证 Provider（验证码认证、用户名密码认证）添加到 ProviderManager 中</li>
     *   <li>认证请求时，ProviderManager 会遍历所有 Provider，找到支持该认证类型的 Provider 进行处理</li>
     *   <li>设置 parent 为 null，避免查找默认的父 AuthenticationManager（防止混淆）</li>
     * </ul>
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>在 {@link CustomAuthenticationFilter} 中调用此管理器进行认证</li>
     *   <li>支持多种认证方式共存（验证码登录、密码登录等）</li>
     * </ul>
     *
     * @return AuthenticationManager 实例
     * @see ProviderManager
     * @see CaptchaAuthenticationProvider
     * @see #daoAuthenticationProvider()
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        ProviderManager providerManager = new ProviderManager(
                Arrays.asList(
                        captchaAuthenticationProvider,
                        daoAuthenticationProvider()
                )
        );
        log.info("自定义AuthenticationManager已创建，包含 {} 个Provider", providerManager.getProviders().size());
        return providerManager;
    }

    /**
     * 密码编码器
     *
     * <p>负责密码的加密和验证，使用 BCrypt 强哈希算法。
     *
     * <p><b>实现原理：</b>
     * <ul>
     *   <li>使用 {@link BCryptPasswordEncoder}，它是 BCrypt 算法的实现</li>
     *   <li>BCrypt 算法会自动加盐，每次加密结果都不同，提高安全性</li>
     *   <li>加密结果包含版本、强度和盐值信息，可自行验证</li>
     * </ul>
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li><b>用户注册：</b>使用 {@link PasswordEncoder#encode(CharSequence)} 对原始密码加密存储</li>
     *   <li><b>用户登录：</b>使用 {@link PasswordEncoder#matches(CharSequence, String)} 比对原始密码和加密密码</li>
     *   <li>在 {@link #daoAuthenticationProvider()} 中作为密码验证器</li>
     * </ul>
     *
     * @return PasswordEncoder 实例
     * @see BCryptPasswordEncoder
     * @see #daoAuthenticationProvider()
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}