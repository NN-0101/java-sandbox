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
 * 加载用户认证信息和权限数据。是 {@link org.springframework.security.authentication.dao.DaoAuthenticationProvider}
 * 和 {@link com.sandbox.services.living.security.provider.CaptchaAuthenticationProvider} 获取用户信息的数据源。
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li><b>用户查询：</b>根据手机号从数据库查询用户基本信息（用户ID、手机号、加密密码等）</li>
 *   <li><b>权限装配：</b>将用户的角色和权限转换为 Spring Security 的 {@link org.springframework.security.core.GrantedAuthority} 对象</li>
 *   <li><b>状态管理：</b>返回用户账号的状态（是否启用、是否锁定、账号是否过期等）</li>
 *   <li><b>异常处理：</b>当用户不存在时抛出 {@link UsernameNotFoundException}，由认证提供者处理</li>
 * </ul>
 *
 * <p><b>返回的用户信息包含：</b>
 * <ul>
 *   <li><b>用户ID：</b>用于标识用户身份，在 JWT 中存储</li>
 *   <li><b>手机号：</b>作为登录凭证，同时也是 principal</li>
 *   <li><b>加密密码：</b>用于密码模式认证（验证码登录模式下不使用）</li>
 *   <li><b>角色列表：</b>用于 RBAC 权限控制，会自动添加 "ROLE_" 前缀</li>
 *   <li><b>权限列表：</b>用于细粒度权限控制（如 @PreAuthorize("hasPermission(...)")）</li>
 *   <li><b>账号状态：</b>是否启用、账号是否过期、凭证是否过期、是否锁定</li>
 * </ul>
 *
 * <p><b>在认证流程中的位置：</b>
 * <ol>
 *   <li>用户提交登录凭证（手机号+密码 或 手机号+验证码）</li>
 *   <li>认证提供者调用 {@link #loadUserByUsername(String)} 获取用户信息</li>
 *   <li>认证提供者根据获取到的信息进行凭证校验（密码比对或验证码校验）</li>
 *   <li>校验通过后，将用户信息（含权限）封装到 Authentication 对象中，存入 SecurityContext</li>
 * </ol>
 *
 * <p><b>TODO 事项：</b>
 * <ul>
 *   <li><b>接入数据库：</b>从用户表（如 sys_user）查询真实用户数据</li>
 *   <li><b>权限查询：</b>联查用户角色表和权限表，组装完整的权限列表</li>
 *   <li><b>缓存机制：</b>添加 Redis 缓存，减少数据库查询压力</li>
 *   <li><b>异常细化：</b>区分用户不存在、用户已禁用、账号已锁定等不同场景，抛出更具体的异常子类</li>
 *   <li><b>多租户支持：</b>如需多租户，可扩展根据租户 ID 查询用户</li>
 * </ul>
 *
 * @author 0101
 * @see UserDetailsService
 * @see CustomUserDetails
 * @see org.springframework.security.authentication.dao.DaoAuthenticationProvider
 * @see com.sandbox.services.living.security.provider.CaptchaAuthenticationProvider
 * @since 2026-03-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * 根据用户名（手机号）加载用户信息
     *
     * <p><b>处理流程：</b>
     * <ol>
     *   <li><b>参数校验：</b>对 username 进行非空和格式校验（可选）</li>
     *   <li><b>数据库查询：</b>【TODO】根据手机号从数据库查询用户信息</li>
     *   <li><b>权限查询：</b>【TODO】根据用户ID查询角色和权限列表</li>
     *   <li><b>构建 UserDetails：</b>使用 {@link CustomUserDetails} 构建用户详情对象</li>
     *   <li><b>异常处理：</b>用户不存在时抛出 {@link UsernameNotFoundException}</li>
     * </ol>
     *
     * <p><b>注意：</b>返回的 {@link CustomUserDetails} 必须包含加密后的密码字段，
     * 因为 {@link org.springframework.security.authentication.dao.DaoAuthenticationProvider}
     * 需要使用该密码进行比对。如果仅使用验证码登录，密码字段可设置为 null 或空字符串。
     *
     * @param username 用户名（手机号）
     * @return 包含用户认证信息和权限的 UserDetails 对象
     * @throws UsernameNotFoundException 当用户不存在时抛出此异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO 从数据库获取用户信息
        // 示例：User user = userMapper.selectByPhone(username);
        // if (user == null) {
        //     throw new UsernameNotFoundException("用户不存在");
        // }

        // TODO 查询用户权限
        // List<String> roles = roleMapper.selectRolesByUserId(user.getId());
        // List<String> permissions = permissionMapper.selectPermissionsByUserId(user.getId());

        // 当前为测试数据，仅用于演示
        // 密码原文为 "123456"，已使用 BCrypt 加密
        log.debug("加载用户信息 - username: {}", username);

        return CustomUserDetails.builder()
                .userId("438929034284234224")                    // 用户ID（模拟数据）
                .phone("18607205429")                             // 手机号（模拟数据）
                .password("$2a$10$KeT6xJQD2GL5S8Kdh2eNo.GxhCgxppgcGgLU3Mof.t2xsCwA8QmYG") // 加密后的密码（123456）
                .enabled(true)                                    // 账号是否启用
                .accountNonExpired(true)                          // 账号是否未过期
                .accountNonLocked(true)                           // 账号是否未锁定
                .credentialsNonExpired(true)                      // 凭证（密码）是否未过期
                .roles(List.of("12"))                              // 角色列表（模拟数据）
                .permissions(List.of("34"))                        // 权限列表（模拟数据）
                .build();
    }

    /**
     * 测试方法：生成 BCrypt 加密密码
     *
     * <p>可用于生成初始用户的加密密码，或测试密码比对。
     * 实际开发中不建议保留此方法，或将其移至测试类。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        // 对 "123456" 进行加密，输出加密后的字符串
        System.out.println(encoder.encode("123456"));
    }
}