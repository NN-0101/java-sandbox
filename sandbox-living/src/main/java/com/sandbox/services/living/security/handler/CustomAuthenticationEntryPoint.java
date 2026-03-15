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
 * 未认证请求的入口点处理器
 *
 * <p>当未认证的用户尝试访问需要认证的资源时，会触发此处理器。
 * 这是 Spring Security 过滤器链中处理认证异常的最后一个环节。</p>
 *
 * <p><b>主要触发场景：</b></p>
 * <ul>
 *   <li><b>未携带认证凭证：</b>请求头中没有 JWT Token 或 Session</li>
 *   <li><b>认证凭证无效：</b>Token 过期、签名错误、格式不正确等</li>
 *   <li><b>匿名用户访问：</b>未登录用户访问需要认证的接口：
 *       <pre>{@code
 *       .authorizeHttpRequests(auth -> auth
 *           .requestMatchers("/api/user/**").authenticated()  // 未认证用户访问这里会触发
 *       )}</pre>
 *   </li>
 *   <li><b>过滤器链中的认证异常：</b>在 {@link org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter}
 *       或自定义过滤器 {@code CustomAuthenticationFilter} 中捕获的认证失败</li>
 * </ul>
 *
 * <p><b>技术实现原理：</b></p>
 * <ul>
 *   <li>当 {@link org.springframework.security.web.access.ExceptionTranslationFilter} 捕获到
 *       {@link AuthenticationException} 时，会调用此处理器</li>
 *   <li>处理器向客户端返回 401 Unauthorized 状态码和标准化的错误响应</li>
 *   <li>与 {@link CustomAccessDeniedHandler}（处理 403）形成完整的异常处理体系</li>
 * </ul>
 *
 * <p><b>与其他处理器的对比：</b></p>
 * <ul>
 *   <li>{@link CustomAuthenticationEntryPoint} - 处理 401 未认证（没有登录）</li>
 *   <li>{@link CustomAccessDeniedHandler} - 处理 403 权限不足（已登录但没权限）</li>
 *   <li>{@link SecurityExceptionHandler} - 处理方法层面的权限异常（如 @PreAuthorize 失败）</li>
 * </ul>
 *
 * @author 0101
 * @see AuthenticationEntryPoint
 * @see org.springframework.security.web.access.ExceptionTranslationFilter
 * @see org.springframework.security.core.AuthenticationException
 * @since 2026-03-14
 */
@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * 处理未认证的访问请求
     *
     * <p>当检测到未认证的访问时，此方法会被调用，返回统一的 401 错误响应。</p>
     *
     * @param request 当前的 HTTP 请求对象
     * @param response 当前的 HTTP 响应对象
     * @param authException 导致认证失败的异常信息，包含具体的失败原因
     * @throws IOException 写入响应时可能发生的 I/O 异常
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.error("【未认证访问】- URI: {}, 认证失败原因: {}", request.getRequestURI(), authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(),
                R.fail(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(), null));
    }
}