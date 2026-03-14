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
 * @description: 用户信息映射类
 * @author: 0101
 * @create: 2026/03/14
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
