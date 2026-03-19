package com.sandbox.services.living.security.filter;

import com.sandbox.services.living.security.token.model.AuthorizationTokenBO;
import com.sandbox.services.living.security.user.CustomUserDetails;
import com.sandbox.services.living.service.AuthorizationTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
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

import java.io.IOException;

/**
 * 自定义 JWT 认证过滤器
 *
 * <p>该过滤器是 Spring Security 认证流程的核心组件，负责拦截所有 HTTP 请求，
 * 从请求头中提取 JWT 令牌，验证令牌有效性，并将认证信息注入到 SecurityContext 中。
 *
 * <p><b>设计特点：</b>
 * <ul>
 *   <li>继承 {@link OncePerRequestFilter}：确保每个请求只执行一次过滤，避免在请求转发（forward）时重复执行</li>
 *   <li><b>无状态认证：</b>不依赖 HttpSession，每次请求都独立验证令牌，符合 RESTful API 设计原则</li>
 *   <li><b>前置认证：</b>在 Spring Security 的过滤器链早期执行，为后续的授权拦截器提供认证信息</li>
 *   <li>通过 {@link Order#value()} 指定过滤器执行顺序，确保在关键过滤器之前执行</li>
 * </ul>
 *
 * <p><b>工作流程：</b>
 * <ol>
 *   <li>从 HTTP 请求头 "Authorization" 中提取 Bearer 令牌</li>
 *   <li>调用 {@link AuthorizationTokenService#validateToken(String)} 验证令牌有效性</li>
 *   <li>验证通过后，将令牌中的用户信息转换为 {@link CustomUserDetails} 对象</li>
 *   <li>创建 {@link UsernamePasswordAuthenticationToken} 认证令牌</li>
 *   <li>将请求详情（IP、SessionID 等）设置到认证令牌中</li>
 *   <li>将认证信息存入 {@link SecurityContextHolder}，供后续使用</li>
 *   <li>继续执行过滤器链</li>
 * </ol>
 *
 * <p><b>触发时机：</b>每个 HTTP 请求到达时<br>
 * <b>处理位置：</b>在 {@link org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter} 之前执行
 *
 * @author 0101
 * @see OncePerRequestFilter
 * @see AuthorizationTokenService
 * @see CustomUserDetails
 * @see UsernamePasswordAuthenticationToken
 * @see SecurityContextHolder
 * @since 2026-03-14
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    /**
     * HTTP 请求头中携带令牌的字段名
     */
    private static final String TOKEN_HEADER = "Authorization";

    /**
     * JWT 令牌的标准前缀（Bearer 类型）
     */
    private static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 授权令牌服务，提供令牌的生成、验证、解析等功能
     */
    private final AuthorizationTokenService authorizationTokenService;

    /**
     * 核心过滤方法
     *
     * <p>每个 HTTP 请求都会经过此方法进行令牌验证和认证信息注入。
     *
     * <p><b>处理步骤：</b>
     * <ol>
     *   <li><b>提取令牌：</b>从请求头 "Authorization" 中获取值，并去除 "Bearer " 前缀</li>
     *   <li><b>验证令牌：</b>调用 {@link AuthorizationTokenService#validateToken(String)} 验证令牌有效性</li>
     *   <li><b>构建用户信息：</b>将令牌中的用户信息（userId、phone 等）封装为 {@link CustomUserDetails}</li>
     *   <li><b>创建认证令牌：</b>使用 {@link UsernamePasswordAuthenticationToken} 封装用户信息和权限</li>
     *   <li><b>添加请求详情：</b>记录客户端 IP、SessionID 等信息，用于审计和监控</li>
     *   <li><b>存入安全上下文：</b>通过 {@link SecurityContextHolder} 将认证信息绑定到当前线程</li>
     *   <li><b>继续过滤：</b>调用 {@link FilterChain#doFilter(ServletRequest, ServletResponse)} 执行后续过滤器</li>
     * </ol>
     *
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>如果请求头中没有令牌或令牌格式不正确，直接放行（由后续的认证入口点处理未认证请求）</li>
     *   <li>如果令牌验证失败（过期、签名错误等），不会设置认证信息，请求将被视为未认证</li>
     *   <li>使用 {@link SecurityContextHolder#getContext().getAuthentication() == null} 检查避免重复认证</li>
     * </ul>
     *
     * @param request     HTTP 请求对象，包含请求头、参数、body 等所有请求信息
     * @param response    HTTP 响应对象，用于返回响应信息
     * @param filterChain 过滤器链，用于继续执行后续过滤器
     * @throws ServletException Servlet 相关异常
     * @throws IOException      IO 异常
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 从请求头获取 Authorization 字段值
        String header = request.getHeader(TOKEN_HEADER);

        // 检查请求头是否存在且以 "Bearer " 开头
        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            // 提取令牌值（去除 "Bearer " 前缀）
            String tokenValue = header.substring(TOKEN_PREFIX.length());

            // 验证令牌有效性
            AuthorizationTokenBO token = authorizationTokenService.validateToken(tokenValue);

            // 如果令牌有效且当前 SecurityContext 中尚未存在认证信息
            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // ========== 构建 UserDetails 对象 ==========
                // 将令牌中的用户信息映射到 Spring Security 的 UserDetails 接口
                CustomUserDetails userDetails = CustomUserDetails.builder()
                        .userId(token.getAccessTokenValue().getUserId())
                        .phone(token.getAccessTokenValue().getPhone())
                        .enabled(true)
                        .accountNonExpired(true)
                        .accountNonLocked(true)
                        .credentialsNonExpired(true)
                        .build();

                // ========== 创建认证令牌 ==========
                // UsernamePasswordAuthenticationToken 是 Spring Security 的标准认证令牌实现
                // 构造参数说明：
                // - principal: 用户主体（这里是 CustomUserDetails）
                // - credentials: 凭证（令牌认证后设置为 null，避免在内存中保留敏感信息）
                // - authorities: 用户权限集合（通过 userDetails.getAuthorities() 获取）
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // ========== 添加请求详情 ==========
                // 将 HTTP 请求的详细信息（IP、SessionID 等）设置到认证令牌中
                // 用途：
                // - 审计日志记录用户访问 IP
                // - 安全监控（异常 IP 检测）
                // - 会话管理
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // ========== 将认证信息存入安全上下文 ==========
                // SecurityContextHolder 是 Spring Security 存储当前认证用户信息的容器
                // 使用 ThreadLocal 实现，因此只在当前请求线程中有效
                // 设置后，后续代码可以通过 SecurityContextHolder.getContext().getAuthentication() 获取当前用户
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("用户认证成功 - userId: {}, phone: {}, requestURI: {}",
                        userDetails.getUserId(), userDetails.getPhone(), request.getRequestURI());
            }
        }

        // 继续执行过滤器链（无论是否认证成功，都放行）
        filterChain.doFilter(request, response);
    }
}