package com.sandbox.services.living.config;

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
 * @description: 安全配置类
 * @author: 0101
 * @create: 2026/03/14
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
     * 作用：配置所有的安全规则，包括：
     * 1. CSRF保护 - 禁用（因为使用JWT无状态认证）
     * 2. 会话管理 - 设置为无状态（STATELESS），不使用HttpSession存储安全上下文
     * 3. 异常处理 - 配置认证失败和权限不足的自定义处理器
     * 4. 请求授权 - 配置哪些接口需要认证，哪些可以匿名访问
     * 5. 认证提供者 - 注册自定义的验证码认证Provider和用户名密码认证Provider
     * 6. 过滤器链 - 在UsernamePasswordAuthenticationFilter之前添加自定义认证过滤器
     *
     * @param http HttpSecurity对象，用于构建过滤器链
     * @return SecurityFilterChain 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // 设置会话为无状态
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 配置异常处理
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                // 配置授权规则
                .authorizeHttpRequests(auth -> auth
                        // 放行登录接口
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/captcha/send").permitAll()
                        .requestMatchers("/api/auth/captcha/login").permitAll()
                        .requestMatchers("/api/auth/refresh").permitAll()
                        // 放行Swagger等文档接口
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // 测试接口需要认证
                        .requestMatchers("/test/**").authenticated()
                        // 其他请求需要认证
                        .anyRequest().authenticated()
                )
                // 添加过滤器
                .addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 关键修改：使用authenticationProvider方法注册自定义Provider
        http.authenticationProvider(captchaAuthenticationProvider);
        http.authenticationProvider(daoAuthenticationProvider());

        return http.build();
    }

    /**
     * 用户名密码认证提供者
     * 作用：处理基于用户名和密码的认证请求
     * 实现原理：
     * 1. 使用DaoAuthenticationProvider，它是Spring Security默认的认证提供者
     * 2. 设置自定义的UserDetailsService，用于根据用户名加载用户信息
     * 3. 设置密码编码器，用于验证用户输入的密码是否与数据库中加密存储的密码匹配
     *
     * @return AuthenticationProvider 认证提供者实例
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
     * 作用：Spring Security认证流程的核心入口，负责协调多个AuthenticationProvider
     * 实现原理：
     * 1. 创建ProviderManager，它是AuthenticationManager的默认实现
     * 2. 将多个认证Provider（验证码认证、用户名密码认证）添加到ProviderManager中
     * 3. 认证请求时，ProviderManager会遍历所有Provider，找到支持该认证类型的Provider进行处理
     * 4. 设置parent为null，避免查找默认的父AuthenticationManager（防止混淆）
     *
     * @return AuthenticationManager 认证管理器实例
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
     * 作用：负责密码的加密和验证
     * 实现原理：
     * 1. 使用BCryptPasswordEncoder，它是BCrypt强哈希算法的实现
     * 2. BCrypt算法会自动加盐，每次加密结果都不同，提高安全性
     * 3. 在用户注册时，使用encode()方法对原始密码进行加密存储
     * 4. 在用户登录时，使用matches()方法比对原始密码和加密密码
     *
     * @return PasswordEncoder 密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}