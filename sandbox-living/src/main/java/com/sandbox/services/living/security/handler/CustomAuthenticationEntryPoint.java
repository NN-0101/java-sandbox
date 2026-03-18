package com.sandbox.services.living.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandbox.services.common.base.vo.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 未认证请求的入口点处理器（401 Unauthorized）
 *
 * <p>当未认证的用户尝试访问需要认证的资源时，会触发此处理器。
 * 这是 Spring Security 过滤器链中处理认证异常的最后一个环节，负责向客户端返回统一的 401 错误响应。
 *
 * <p><b>主要触发场景：</b>
 * <ul>
 *   <li><b>未携带认证凭证：</b>请求头中没有 Authorization 字段或格式不正确</li>
 *   <li><b>认证凭证无效：</b>JWT Token 过期、签名错误、被篡改等</li>
 *   <li><b>匿名用户访问受保护资源：</b>未登录用户访问需要认证的接口：
 *       <pre>{@code
 *       .authorizeHttpRequests(auth -> auth
 *           .requestMatchers("/api/user/**").authenticated()
 *       )}</pre>
 *   </li>
 *   <li><b>自定义认证过滤器抛出异常：</b>如 {@link com.sandbox.services.living.security.filter.CustomAuthenticationFilter}
 *       在解析 Token 时发现无效</li>
 * </ul>
 *
 * <p><b>技术实现原理：</b>
 * <ul>
 *   <li>当 {@link org.springframework.security.web.access.ExceptionTranslationFilter} 捕获到
 *       {@link AuthenticationException} 时，会调用此处理器</li>
 *   <li>处理器向客户端返回 HTTP 401 Unauthorized 状态码和标准化的 JSON 错误响应</li>
 *   <li>响应体使用统一的 {@link R} 格式，包含错误码和错误信息</li>
 * </ul>
 *
 * <p><b>异常处理体系对比：</b>
 * <ul>
 *   <li><b>{@link CustomAuthenticationEntryPoint} - 处理 401 未认证</b>（用户未登录或凭证无效）</li>
 *   <li>{@link com.sandbox.services.living.security.handler.CustomAccessDeniedHandler} - 处理 403 权限不足（用户已登录但缺乏权限）</li>
 *   <li>{@link org.springframework.web.bind.annotation.ControllerAdvice} 全局异常处理器 - 处理方法层面抛出的业务异常</li>
 * </ul>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li>实现 {@link AuthenticationEntryPoint} 接口，融入 Spring Security 标准异常处理机制</li>
 *   <li>记录错误日志，包含请求 URI 和具体的异常信息，便于问题追踪</li>
 *   <li>返回统一的 JSON 格式响应，便于前端统一处理</li>
 *   <li>不暴露具体的认证失败原因（如“token过期”），避免给攻击者提供信息，统一返回“未认证”</li>
 * </ul>
 *
 * @author 0101
 * @see AuthenticationEntryPoint
 * @see org.springframework.security.web.access.ExceptionTranslationFilter
 * @see org.springframework.security.core.AuthenticationException
 * @see com.sandbox.services.living.security.handler.CustomAccessDeniedHandler
 * @since 2026-03-14
 */
@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * 处理未认证的访问请求
     *
     * <p>当 Spring Security 过滤器链中发生 {@link AuthenticationException} 时，
     * {@link org.springframework.security.web.access.ExceptionTranslationFilter} 会调用此方法。
     *
     * <p><b>处理步骤：</b>
     * <ol>
     *   <li><b>日志记录：</b>记录错误级别的日志，包含请求 URI 和认证失败的具体原因</li>
     *   <li><b>设置响应头：</b>设置 Content-Type 为 application/json</li>
     *   <li><b>设置状态码：</b>设置 HTTP 状态码为 401 Unauthorized</li>
     *   <li><b>构建响应体：</b>使用 {@link ObjectMapper} 将 {@link R} 对象序列化为 JSON</li>
     *   <li><b>写入响应：</b>将 JSON 字符串写入响应输出流</li>
     * </ol>
     *
     * @param request        当前的 HTTP 请求对象，包含请求 URI 等信息
     * @param response       当前的 HTTP 响应对象，用于返回错误信息
     * @param authException  导致认证失败的异常信息，包含具体的失败原因（如“JWT expired”）
     * @throws IOException   当写入响应流时可能发生的 I/O 异常
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // 记录错误日志，包含请求路径和具体的认证失败原因
        log.error("【未认证访问】- URI: {}, 认证失败原因: {}", request.getRequestURI(), authException.getMessage());

        // 设置响应内容类型为 JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // 设置 HTTP 状态码为 401 Unauthorized
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 创建 ObjectMapper 用于将对象转换为 JSON
        ObjectMapper mapper = new ObjectMapper();
        // 将统一响应对象写入输出流
        // R.fail() 方法返回一个包含状态码、消息和数据的失败响应对象
        mapper.writeValue(response.getOutputStream(),
                R.fail(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(), null));
    }
}