package com.sandbox.services.living.security.service;

import com.sandbox.services.living.security.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户详情加载服务
 *
 * <p>实现 Spring Security 的 {@link UserDetailsService} 接口，负责根据用户名（手机号）
 * 加载用户认证信息和权限数据。是 DaoAuthenticationProvider 和 CaptchaAuthenticationProvider
 * 获取用户信息的数据源。</p>
 *
 * <p><b>核心职责：</b></p>
 * <ul>
 *   <li><b>用户查询：</b>根据手机号从数据库查询用户信息</li>
 *   <li><b>权限装配：</b>将用户的角色和权限转换为 Spring Security 的 GrantedAuthority 对象</li>
 *   <li><b>状态管理：</b>返回用户账号的状态（是否启用、是否锁定等）</li>
 * </ul>
 *
 * <p><b>返回的用户信息包含：</b></p>
 * <ul>
 *   <li>用户ID：用于标识用户身份</li>
 *   <li>手机号：作为登录凭证</li>
 *   <li>加密密码：用于密码模式认证</li>
 *   <li>角色列表：用于 RBAC 权限控制，会自动添加 "ROLE_" 前缀</li>
 *   <li>权限列表：用于细粒度权限控制</li>
 *   <li>账号状态：是否启用、未过期、未锁定等</li>
 * </ul>
 *
 * <p><b>TODO 事项：</b></p>
 * <ul>
 *   <li>接入数据库查询真实用户数据</li>
 *   <li>添加缓存机制减少数据库查询</li>
 *   <li>完善异常处理，区分用户不存在、用户已禁用等不同场景</li>
 * </ul>
 *
 * @author 0101
 * @see UserDetailsService
 * @see CustomUserDetails
 * @see org.springframework.security.authentication.dao.DaoAuthenticationProvider
 * @since 2026-03-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO 从数据库获取用户信息
        return CustomUserDetails.builder()
                .userId("438929034284234224")
                .phone("18607205429")
                .password("$2a$10$KeT6xJQD2GL5S8Kdh2eNo.GxhCgxppgcGgLU3Mof.t2xsCwA8QmYG") // 加密后的密码
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .roles(List.of("12"))
                .permissions(List.of("34"))
                .build();
    }

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("123456"));
    }
}
