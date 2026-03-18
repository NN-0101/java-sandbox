package com.sandbox.services.living.security.handler;

import com.sandbox.services.common.base.vo.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Controller 方法层面的权限异常处理器
 *
 * <p>处理在 Controller 方法执行过程中发生的权限拒绝异常，主要场景包括：
 * <ul>
 *   <li>通过 {@code @PreAuthorize} 注解进行的方法级别权限控制：
 *       <pre>{@code
 *       @PreAuthorize("hasRole('ADMIN')")
 *       @GetMapping("/admin/data")
 *       public Data getAdminData() { ... }
 *
 *       @PreAuthorize("@securityService.checkPermission(#id)")
 *       @DeleteMapping("/user/{id}")
 *       public void deleteUser(@PathVariable Long id) { ... }
 *       }</pre>
 *   </li>
 *   <li>通过 {@code @PostAuthorize}、{@code @PreFilter}、{@code @PostFilter} 等注解进行的权限控制</li>
 *   <li>使用 {@code @Secured} 或 {@code @RolesAllowed} 注解的权限控制</li>
 *   <li>在 Service 层或 Controller 层通过编程式权限检查抛出的 {@link AccessDeniedException}</li>
 * </ul>
 *
 * <p><b>触发时机：</b>请求已到达 Controller 方法，但在执行前/后权限校验失败时<br>
 * <b>处理位置：</b>在 AOP 切面中通过 {@link org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor}
 * 等拦截器抛出异常，最终由 {@code @RestControllerAdvice} 统一捕获处理
 *
 * <p><b>与过滤器层面权限异常的区别：</b>
 * <ul>
 *   <li><b>{@link com.sandbox.services.living.security.handler.CustomAccessDeniedHandler}：</b>
 *       处理过滤器链中的权限异常（如 URL 级别的权限控制 .requestMatchers("/admin/**").hasRole("ADMIN")）</li>
 *   <li><b>本处理器：</b>处理方法级别的权限异常（如 @PreAuthorize 注解）</li>
 *   <li>两者共同构成完整的权限异常处理体系，确保所有类型的权限拒绝都能被妥善处理</li>
 * </ul>
 *
 * <p><b>兼容性说明：</b>
 * <ul>
 *   <li>Spring Security 6.0+ 引入了新的 {@link AuthorizationDeniedException} 作为权限异常基类</li>
 *   <li>旧版本的 {@link AccessDeniedException} 仍然存在，且是新异常的父类</li>
 *   <li>本处理器同时捕获两种异常，确保在不同 Spring Security 版本下都能正常工作</li>
 * </ul>
 *
 * @author 0101
 * @see org.springframework.security.access.prepost.PreAuthorize
 * @see org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor
 * @see com.sandbox.services.living.security.handler.CustomAccessDeniedHandler
 * @since 2026-03-15
 */
@Slf4j
@RestControllerAdvice
public class SecurityExceptionHandler {

    /**
     * 处理方法级别的权限拒绝异常
     *
     * <p>当使用 {@code @PreAuthorize} 等注解进行方法级别权限控制，且当前用户权限不足时，
     * Spring Security 会抛出 {@link AccessDeniedException} 或其子类 {@link AuthorizationDeniedException}，
     * 此方法负责捕获并返回统一的 403 错误响应。
     *
     * <p><b>处理步骤：</b>
     * <ol>
     *   <li><b>日志记录：</b>记录错误日志，包含异常类型和具体的错误信息，便于问题追踪</li>
     *   <li><b>构建响应：</b>使用统一的 {@link R} 响应体，包含 HTTP 403 状态码和错误信息</li>
     *   <li><b>返回响应：</b>包装为 {@link ResponseEntity} 返回，包含 403 状态码</li>
     * </ol>
     *
     * <p><b>示例场景：</b>
     * <pre>
     * &#64;PreAuthorize("hasRole('ADMIN')")
     * &#64;GetMapping("/api/admin/data")
     * public Data getAdminData() {
     *     // 如果当前用户不是 ADMIN 角色，会触发此异常处理器
     *     return adminService.getData();
     * }
     * </pre>
     *
     * @param e 权限拒绝异常，可以是 {@link AccessDeniedException} 或其子类 {@link AuthorizationDeniedException}
     * @return 统一的 403 权限不足响应，包含标准化的错误信息
     */
    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<R<Void>> handleMethodLevelAccessDenied(Exception e) {
        // 记录错误日志，包含异常类型和具体错误信息
        log.error("【方法层面】权限不足 - 类型: {}, 错误信息: {}", e.getClass().getSimpleName(), e.getMessage());

        // 返回 403 Forbidden 响应
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                R.fail(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), null));
    }
}