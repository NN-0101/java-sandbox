package com.sandbox.services.living.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandbox.services.common.base.vo.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 过滤器链层面的权限拒绝处理器
 *
 * <p>处理在 Spring Security 过滤器链中发生的权限拒绝异常，主要场景包括：
 * <ul>
 *   <li>通过 {@code HttpSecurity} 配置的 URL 权限控制：
 *       <pre>{@code
 *       .authorizeHttpRequests(auth -> auth
 *           .requestMatchers("/api/admin/**").hasRole("ADMIN")
 *           .requestMatchers("/api/user/**").hasRole("USER")
 *       )}</pre>
 *   </li>
 *   <li>请求在到达 Controller 之前就被过滤器链拦截的权限不足情况</li>
 * </ul>
 *
 * <p><b>触发时机：</b>用户已认证但缺乏访问特定 URL 的权限时<br>
 * <b>处理位置：</b>在 {@link org.springframework.security.web.access.ExceptionTranslationFilter} 中触发
 *
 * @author 0101
 * @see AccessDeniedHandler
 * @see org.springframework.security.web.access.ExceptionTranslationFilter
 * @since 2026-03-14
 */
@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        log.error("【过滤器层面】权限不足 - URI: {}, 错误信息: {}", request.getRequestURI(), accessDeniedException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(),
                R.fail(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), null));
    }
}