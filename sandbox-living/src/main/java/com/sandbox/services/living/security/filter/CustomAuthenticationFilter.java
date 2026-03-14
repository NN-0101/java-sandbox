package com.sandbox.services.living.security.filter;

import com.sandbox.services.living.security.manager.TokenManager;
import com.sandbox.services.living.security.token.CustomToken;
import com.sandbox.services.living.security.user.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;

/**
 * 自定义认证过滤器
 * 核心职责：拦截所有请求，解析并验证令牌，将认证信息注入Spring Security上下文
 * 设计特点：
 * 1. 继承OncePerRequestFilter - 确保每个请求只执行一次过滤（避免在forward时重复执行）
 * 2. 无状态认证 - 不依赖session，每次请求都独立验证令牌
 * 3. 前置认证 - 在Spring Security的过滤器链早期执行，为后续授权做准备
 *
 * @author 0101
 * @create 2026/03/14
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final TokenManager tokenManager;

    private final RequestMappingHandlerMapping handlerMapping;

    /**
     * 核心过滤方法
     * 每个HTTP请求都会经过这里
     *
     * @param request HTTP请求对象 - 包含请求头、参数、body等所有请求信息
     * @param response HTTP响应对象 - 用于返回响应信息
     * @param filterChain 过滤器链 - 用于继续执行后续过滤器
     * @throws ServletException Servlet相关异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 从请求头获取令牌
        String header = request.getHeader(TOKEN_HEADER);

        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            String tokenValue = header.substring(TOKEN_PREFIX.length());

            // 验证令牌
            CustomToken token = tokenManager.validateToken(tokenValue);

            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // ========== 构建UserDetails对象 ==========
                // 将令牌中的用户信息映射到Spring Security的UserDetails接口
                CustomUserDetails userDetails = CustomUserDetails.builder()
                        .userId(token.getUserId())
                        .phone(token.getUsername())
                        .enabled(true)
                        .accountNonExpired(true)
                        .accountNonLocked(true)
                        .credentialsNonExpired(true)
                        .build();

                // ========== 创建认证令牌 ==========
                // UsernamePasswordAuthenticationToken是Spring Security的标准认证令牌实现
                // 构造参数说明：
                // 参数1 - principal: 用户主体（这里是CustomUserDetails）
                // 参数2 - credentials: 凭证（令牌认证后设置为null，因为不需要在内存中保留敏感信息）
                // 参数3 - authorities: 用户权限集合（这里使用userDetails.getAuthorities()获取）
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // ========== 添加请求详情 ==========
                // 将HTTP请求的详细信息（IP、SessionID等）设置到认证令牌中
                // 用途：
                // 1. 审计日志记录用户访问IP
                // 2. 安全监控（异常IP检测）
                // 3. 会话管理
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // ========== 将认证信息存入安全上下文 ==========
                // SecurityContextHolder是Spring Security存储当前认证用户信息的容器
                // 这是一个ThreadLocal，所以只在当前请求线程中有效
                // 设置后，后续的代码可以通过SecurityContextHolder.getContext().getAuthentication()获取当前用户
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
