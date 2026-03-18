package com.sandbox.services.living.security.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 自定义用户详情实现类
 *
 * <p>实现 Spring Security 的 {@link UserDetails} 接口，封装当前登录用户的完整信息，
 * 包括用户身份数据、账号状态和权限集合。该类是认证和授权过程中的核心数据载体，
 * 在用户成功认证后，被存储在 {@link org.springframework.security.core.context.SecurityContext} 中。
 *
 * <p><b>数据组成：</b>
 * <ul>
 *   <li><b>用户身份：</b>userId（用户ID）、phone（手机号/用户名）</li>
 *   <li><b>认证凭证：</b>password（加密后的密码，仅用于密码模式认证）</li>
 *   <li><b>账号状态：</b>enabled（是否启用）、accountNonExpired（账号是否未过期）、
 *       accountNonLocked（账号是否未锁定）、credentialsNonExpired（凭证是否未过期）</li>
 *   <li><b>权限数据：</b>roles（角色列表）、permissions（权限列表）</li>
 * </ul>
 *
 * <p><b>权限处理规则：</b>
 * <ul>
 *   <li><b>角色（Role）：</b>自动添加 "ROLE_" 前缀，如 role="ADMIN" 转换为 "ROLE_ADMIN"，
 *       便于 Spring Security 的 hasRole('ADMIN') 方法识别</li>
 *   <li><b>权限（Permission）：</b>直接作为权限标识，如 permission="user:delete"，
 *       可用于 hasAuthority('user:delete') 或 @PreAuthorize 表达式</li>
 *   <li>在 {@link #getAuthorities()} 方法中合并角色和权限，供授权决策使用</li>
 * </ul>
 *
 * <p><b>账号状态默认值：</b>
 * <ul>
 *   <li>所有状态字段默认值为 true（有效状态），避免空指针异常</li>
 *   <li>可通过数据库字段控制账号的启用/禁用、锁定等状态，如用户被管理员禁用时 enabled = false</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>在 {@link com.sandbox.services.living.security.service.CustomUserDetailsService#loadUserByUsername(String)}
 *       中构建并返回该对象</li>
 *   <li>在 Controller 或 Service 层，可通过 SecurityContextHolder.getContext().getAuthentication().getPrincipal()
 *       获取当前登录用户信息</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>
 * // 构建用户详情
 * CustomUserDetails user = CustomUserDetails.builder()
 *     .userId("10001")
 *     .phone("13800138000")
 *     .password(encodedPassword)
 *     .enabled(true)
 *     .accountNonExpired(true)
 *     .accountNonLocked(true)
 *     .credentialsNonExpired(true)
 *     .roles(List.of("ADMIN", "USER"))
 *     .permissions(List.of("system:config:read", "system:config:write"))
 *     .build();
 *
 * </pre>
 *
 * @author 0101
 * @see UserDetails
 * @see org.springframework.security.core.authority.SimpleGrantedAuthority
 * @see com.sandbox.services.living.security.service.CustomUserDetailsService
 * @see org.springframework.security.core.context.SecurityContextHolder
 * @since 2026-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    /**
     * 用户ID
     *
     * <p>业务系统中的唯一用户标识，可用于关联用户数据、记录操作日志等。
     */
    private String userId;

    /**
     * 手机号
     *
     * <p>作为登录用户名（principal），同时也是用户的联系方式。
     * 在验证码登录模式中，手机号既是登录凭证也是用户标识。
     */
    private String phone;

    /**
     * 加密后的密码
     *
     * <p>使用 BCrypt 等算法加密存储的密码，仅用于密码模式认证。
     * 验证码登录模式下不使用此字段。
     */
    private String password;

    /**
     * 账号是否启用
     *
     * <p>false 表示账号被禁用，无法登录系统。
     * 例如：用户被管理员封禁、账号未激活等场景。
     */
    private Boolean enabled;

    /**
     * 账号是否未过期
     *
     * <p>false 表示账号已过期，无法登录系统。
     * 例如：会员账号过期、试用期结束等场景。
     */
    private Boolean accountNonExpired;

    /**
     * 账号是否未锁定
     *
     * <p>false 表示账号被锁定，无法登录系统。
     * 例如：多次密码错误导致的临时锁定、管理员手动锁定等场景。
     */
    private Boolean accountNonLocked;

    /**
     * 凭证（密码）是否未过期
     *
     * <p>false 表示密码已过期，需要修改密码后才能登录。
     * 例如：密码定期过期策略、强制修改初始密码等场景。
     */
    private Boolean credentialsNonExpired;

    /**
     * 角色列表
     *
     * <p>用户的角色集合，用于 RBAC（基于角色的访问控制）模型。
     * 在转换时会自动添加 "ROLE_" 前缀。
     */
    private List<String> roles;

    /**
     * 权限列表
     *
     * <p>用户的权限集合，用于细粒度的权限控制。
     * 权限可以是资源操作标识，如 "user:create"、"order:delete" 等。
     */
    private List<String> permissions;

    /**
     * 获取用户的所有权限（角色+权限）
     *
     * <p>将 roles 和 permissions 合并为 {@link GrantedAuthority} 集合，
     * 供 Spring Security 的授权机制使用。
     *
     * <p><b>转换规则：</b>
     * <ul>
     *   <li>roles 中的每个元素添加 "ROLE_" 前缀，包装为 {@link SimpleGrantedAuthority}</li>
     *   <li>permissions 中的每个元素直接包装为 {@link SimpleGrantedAuthority}</li>
     * </ul>
     *
     * @return 权限集合，如果 roles 和 permissions 都为空则返回空集合
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 如果 roles 和 permissions 都为空，返回空集合
        if (roles == null && permissions == null) {
            return Collections.emptyList();
        }

        // 创建权限集合
        List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();

        // 处理角色（添加 ROLE_ 前缀）
        if (roles != null) {
            authorities.addAll(roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .toList());
        }

        // 处理权限（直接转换）
        if (permissions != null) {
            authorities.addAll(permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList());
        }

        return authorities;
    }

    /**
     * 获取加密后的密码
     *
     * @return 密码字符串
     */
    @Override
    public String getPassword() {
        return this.password;
    }

    /**
     * 获取用户名（手机号）
     *
     * @return 手机号字符串
     */
    @Override
    public String getUsername() {
        return this.phone;
    }

    /**
     * 判断账号是否未过期
     *
     * @return true 表示未过期，false 表示已过期
     */
    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired != null ? this.accountNonExpired : true;
    }

    /**
     * 判断账号是否未锁定
     *
     * @return true 表示未锁定，false 表示已锁定
     */
    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked != null ? this.accountNonLocked : true;
    }

    /**
     * 判断凭证（密码）是否未过期
     *
     * @return true 表示未过期，false 表示已过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired != null ? this.credentialsNonExpired : true;
    }

    /**
     * 判断账号是否启用
     *
     * @return true 表示启用，false 表示禁用
     */
    @Override
    public boolean isEnabled() {
        return this.enabled != null ? this.enabled : true;
    }
}