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
 * @description: 用户详情服务
 * @author: 0101
 * @create: 2026/03/14
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
                .password("$2a$10$gmiKfHXCRmpf.bop7MT9w.SSWsioJ1gHiDuQZbDto2dbVPll6t/42") // 加密后的密码
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
