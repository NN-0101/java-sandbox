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
 * 包括用户身份数据、账号状态和权限集合。该类是认证和授权过程中的核心数据载体。</p>
 *
 * <p><b>数据组成：</b></p>
 * <ul>
 *   <li><b>用户身份：</b>userId（用户ID）、phone（手机号/用户名）</li>
 *   <li><b>认证凭证：</b>password（加密后的密码）</li>
 *   <li><b>账号状态：</b>enabled（是否启用）、accountNonExpired（未过期）、
 *       accountNonLocked（未锁定）、credentialsNonExpired（凭证未过期）</li>
 *   <li><b>权限数据：</b>roles（角色列表）、permissions（权限列表）</li>
 * </ul>
 *
 * <p><b>权限处理规则：</b></p>
 * <ul>
 *   <li>角色（Role）：自动添加 "ROLE_" 前缀，如 role="ADMIN" 转换为 "ROLE_ADMIN"</li>
 *   <li>权限（Permission）：直接作为权限标识，如 permission="user:delete"</li>
 *   <li>在 {@link #getAuthorities()} 方法中合并角色和权限，供授权决策使用</li>
 * </ul>
 *
 * <p><b>账号状态默认值：</b></p>
 * <ul>
 *   <li>所有状态字段默认值为 true（有效状态）</li>
 *   <li>可通过数据库字段控制账号的启用/禁用、锁定等状态</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * CustomUserDetails user = CustomUserDetails.builder()
 *     .userId("10001")
 *     .phone("13800138000")
 *     .password(encodedPassword)
 *     .enabled(true)
 *     .roles(List.of("ADMIN", "USER"))
 *     .permissions(List.of("system:config:read", "system:config:write"))
 *     .build();
 * </pre>
 *
 * @author 0101
 * @see UserDetails
 * @see org.springframework.security.core.authority.SimpleGrantedAuthority
 * @see com.sandbox.services.living.security.service.CustomUserDetailsService
 * @since 2026-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private String userId;
    private String phone;
    private String password;
    private Boolean enabled;
    private Boolean accountNonExpired;
    private Boolean accountNonLocked;
    private Boolean credentialsNonExpired;
    private List<String> roles;
    private List<String> permissions;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 合并角色和权限
        if (roles == null && permissions == null) {
            return Collections.emptyList();
        }

        // 角色需要添加 ROLE_ 前缀
        List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();

        if (roles != null) {
            authorities.addAll(roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .toList());
        }

        if (permissions != null) {
            authorities.addAll(permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList());
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.phone;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired != null ? this.accountNonExpired : true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked != null ? this.accountNonLocked : true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired != null ? this.credentialsNonExpired : true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled != null ? this.enabled : true;
    }

}
