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
 * @description: 验证码认证Provider
 * @author: 0101
 * @create: 2026/03/14
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