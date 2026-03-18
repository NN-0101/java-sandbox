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
 * <p><b>与 Controller 层权限异常的区别：</b>
 * <ul>
 *   <li>该处理器处理的是过滤器链中的权限异常（如 URL 级别的权限控制）</li>
 *   <li>而方法级别的权限异常（如 {@code @PreAuthorize}）会抛出不同的异常，通常由全局异常处理器处理</li>
 * </ul>
 *
 * <p><b>处理逻辑：</b>
 * <ol>
 *   <li>记录错误日志，包含请求 URI 和具体的权限异常信息</li>
 *   <li>设置响应内容类型为 JSON</li>
 *   <li>设置 HTTP 状态码为 403（禁止访问）</li>
 *   <li>使用 {@link ObjectMapper} 将统一的响应体 {@link R} 写入响应流</li>
 * </ol>
 *
 * @author 0101
 * @see AccessDeniedHandler
 * @see org.springframework.security.web.access.ExceptionTranslationFilter
 * @see org.springframework.security.access.prepost.PreAuthorize
 * @since 2026-03-14
 */
@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * 处理权限不足的异常
     *
     * <p>当用户已通过认证，但尝试访问其角色或权限不允许的资源时，
     * Spring Security 会抛出 {@link AccessDeniedException} 并调用此方法进行处理。
     *
     * <p><b>处理步骤：</b>
     * <ul>
     *   <li><b>日志记录：</b>记录错误级别的日志，包含请求 URI 和异常详情，便于问题追踪</li>
     *   <li><b>响应封装：</b>构建统一的 JSON 格式错误响应，包含 HTTP 403 状态码和错误信息</li>
     *   <li><b>输出响应：</b>将响应内容写入 HttpServletResponse 的输出流</li>
     * </ul>
     *
     * @param request               HTTP 请求对象，包含请求 URI 等信息
     * @param response              HTTP 响应对象，用于返回错误信息
     * @param accessDeniedException 权限拒绝异常，包含具体的错误原因
     * @throws IOException 当写入响应流时可能发生的 I/O 异常
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        // 记录错误日志，包含请求路径和异常信息
        log.error("【过滤器层面】权限不足 - URI: {}, 错误信息: {}", request.getRequestURI(), accessDeniedException.getMessage());

        // 设置响应内容类型为 JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // 设置 HTTP 状态码为 403 Forbidden
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        // 创建 ObjectMapper 用于将对象转换为 JSON
        ObjectMapper mapper = new ObjectMapper();
        // 将统一响应对象写入输出流
        // R.fail() 方法返回一个包含状态码、消息和数据的失败响应对象
        mapper.writeValue(response.getOutputStream(),
                R.fail(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), null));
    }
}